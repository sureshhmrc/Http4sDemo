package io.sure360

import http4sDemo.config.Config
import http4sDemo.db.Database
import http4sDemo.model.Customer
import http4sDemo.model.Customer._
import http4sDemo.repository.CustomerRepository

import cats.effect._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{HttpApp, HttpRoutes, Uri}

import scala.concurrent.ExecutionContext.global

object HttpServer extends Http4sDsl[IO] {

  def customerRoutes(repository: CustomerRepository): HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case GET -> Root / "customer" / LongVar(id) =>
        for {
          dbResult <- repository.get(id)
          response <- createResponse(dbResult)
        } yield {
          response
        }

      case req@POST -> Root / "customer" =>
        for {
          customer <- req.decodeJson[Customer]
          createdCustomer <- repository.createCustomer(customer)
          response <- Created(createdCustomer.asJson, Location(Uri.unsafeFromString(s"/customer/${createdCustomer.id.get}")))
        } yield response
    }
  }

  def allRoutes(repository: CustomerRepository): HttpApp[IO] =
    customerRoutes(repository).orNotFound

  def create(configFile: String = "application.conf"): IO[ExitCode] = {
    resources(configFile).use(create)
  }

  private def resources(configFile: String): Resource[IO, Resources] = {
    for {
      config <- Config.load(configFile)
      ec <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      transactor <- Database.transactor(config.database, ec)
    } yield Resources(transactor, config)
  }

  def createResponse(dbResult: Either[Customer.CustomerNotFound.type, Customer]) =
    dbResult match {
      case Left(CustomerNotFound) => NotFound()
      case Right(customer) => Ok(customer.asJson)
    }

  private def create(resources: Resources): IO[ExitCode] = {
    for {
      _ <- Database.initialize(resources.transactor)
      repository = new CustomerRepository(resources.transactor)
      exitCode <-
        BlazeServerBuilder[IO](global)
          .bindHttp(resources.config.server.port, resources.config.server.host)
          .withHttpApp(allRoutes(repository))
          .resource
          .use(_ => IO.never)
          .as(ExitCode.Success)
    } yield exitCode
  }

  case class Resources(transactor: HikariTransactor[IO], config: Config)
}
