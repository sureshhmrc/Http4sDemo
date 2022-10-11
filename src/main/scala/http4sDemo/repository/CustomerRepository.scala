package io.sure360
package http4sDemo.repository

import http4sDemo.model.Customer
import http4sDemo.model.Customer.CustomerNotFound

import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor

class CustomerRepository(transactor: Transactor[IO]) {

  def get(id: Long): IO[Either[CustomerNotFound.type, Customer]] = {
    sql"SELECT id, name FROM customer WHERE id = $id".query[Customer]
      .option
      .transact(transactor)
      .map {
        case Some(customer) => Right(customer)
        case None => Left(CustomerNotFound)
      }
  }

  def createCustomer(customer: Customer): IO[Customer] = {
    sql"INSERT INTO customer (name) VALUES (${customer.name})".update
      .withUniqueGeneratedKeys[Long]("id").transact(transactor).map { id =>
      customer.copy(id = Some(id))
    }
  }
}
