package io.sure360
package http4sDemo.model

case class Customer(id: Option[Long], name: String)

object Customer {
  //implicit val customerGet: Get[Customer] = deriving
  //implicit val customerPut: Put[Customer] = deriving

  case object CustomerNotFound extends Exception
}
