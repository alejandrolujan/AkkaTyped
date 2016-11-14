package com.example

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

import akka.typed.AskPattern._
import akka.typed.ScalaDSL._
import akka.typed._
import akka.util.Timeout


sealed trait UserCacheMessage
case class AddUser(name: String) extends UserCacheMessage
case class RemoveUser(name: String) extends UserCacheMessage
case class ListUsers(replyTo: ActorRef[Users]) extends UserCacheMessage

case class Users(users: Set[String])

object UserCacheApp extends App {

  def userCache(users: Set[String]): Behavior[UserCacheMessage] =
    Total[UserCacheMessage] {
      case AddUser(name) =>
        println(s"Adding user $name")
        userCache(users + name)
      case RemoveUser(name) =>
        println(s"Removing user $name")
        userCache(users - name)
      case ListUsers(replyTo) =>
        println(s"Sending back user set: $users")
        replyTo ! Users(users)
        Same
      }

  val system = ActorSystem("UserCacheSystem", userCache(Set.empty))

  system ! AddUser("Frodo")
  system ! AddUser("Sam")
  system ! AddUser("Boromir")
  system ! RemoveUser("Boromir")

  implicit val timeout = Timeout(1 second)
  implicit val scheduler = system.scheduler
  val fUsers: Future[Users] = system ? ListUsers

  val terminated =
    fUsers.map { users =>
      println(s"User set: '$users'")
      system.terminate()
    }

  Await.result(terminated, 2 seconds)

}
