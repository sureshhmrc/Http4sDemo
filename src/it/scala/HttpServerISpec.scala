package io.sure360

import http4sDemo.config.Config

import cats.effect.unsafe.IORuntime
import cats.effect.{ExitCode, IO}
import io.circe.Json
import io.circe.literal._
import io.circe.optics.JsonPath._
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.global

class HttpServerISpec extends AnyWordSpec with Matchers with BeforeAndAfterAll with Eventually {
  private lazy val client = BlazeClientBuilder[IO](global).resource

  private val configFile = "test.conf"

  private lazy val config = Config.load(configFile).use(config => IO.pure(config)).unsafeRunSync()

  private lazy val urlStart = s"http://${config.server.host}:${config.server.port}"

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = scaled(Span(5, Seconds)), interval = scaled(Span(100, Millis)))

  private implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  private val logger = LoggerFactory.getLogger(classOf[HttpServerISpec])

  override def beforeAll(): Unit = {
    HttpServer.create(configFile).unsafeRunAsync(resultHandler)
  }

  "Http server" should {
    val name = "John"
    val createJson = json"""{"name": $name}"""

    "create a customer" in {
      val request = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$urlStart/customer")).withEntity(createJson)
      val json = client.use(_.expect[Json](request)).unsafeRunSync()
      root.id.long.getOption(json).nonEmpty shouldBe true
      root.name.string.getOption(json) shouldBe Some(name)
    }

    "return a single customer" in {

      val request = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$urlStart/customer")).withEntity(createJson)
      val json = client.use(_.expect[Json](request)).unsafeRunSync()
      root.id.long.getOption(json).nonEmpty shouldBe true
      root.name.string.getOption(json) shouldBe Some(name)

      val id = root.id.long.getOption(json).get

      client.use(_.expect[Json](Uri.unsafeFromString(s"$urlStart/customer/$id"))).unsafeRunSync() shouldBe
        json"""
        {
          "id": $id,
          "name": $name
        }"""
    }
  }

  private def resultHandler(result: Either[Throwable, ExitCode]): Unit = {
    result.left.foreach(t => logger.error("Executing the http server failed", t))
  }
}
