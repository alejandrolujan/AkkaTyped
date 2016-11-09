package com.example

import akka.typed._
import akka.typed.ScalaDSL._
import akka.typed.AskPattern._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

import akka.util.Timeout

// The simplest app one can write with Akka Typed.
// Objective: write a behavior that receives a Message with a String payload and responds
// with an Echo with the same payload
object EchoApp extends App {

  // Incoming and outgoing message types
  case class Message(payload: String, replyTo: ActorRef[Echo])
  case class Echo(payload: String)

  val echo = Static[Message] { message: Message =>
    println(s"Received this: '$message'")
    // Note that we cannot send anything else but Echo through this ! operator
    message.replyTo ! Echo(message.payload)
  }

  // The actor system we'll use to host the Echo actor
  val system: ActorSystem[Message] = ActorSystem("EchoSystem", echo)

  // TODO this seems to be missing from docs:
  implicit val timeout = Timeout(1 second)
  implicit val scheduler = system.scheduler

  // Note the Future is already typed, since we know what the system will respond with
  val future: Future[Echo] = system ? (Message("Hola", _))

  val terminated =
    future.map { echo: Echo =>
      println(s"Got this back: '$echo'")
      system.terminate()
    }

  // TODO missing from docs
  Await.result(terminated, 2 seconds)

}
