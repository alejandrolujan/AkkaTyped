package com.example

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.{Props, ActorSystem, Actor}

case class Envelope(content: String)

class MyActor extends Actor {
  override def receive: Receive = {
    case Envelope =>
      println("Received an envelope!")
  }
}

object UntypedActorsApp extends App {

  val system = ActorSystem("MyActorSystem")
  val ref = system.actorOf(Props[MyActor])
  ref ! Envelope("Hello")

  Await.result(system.terminate(), 1 second)
}

