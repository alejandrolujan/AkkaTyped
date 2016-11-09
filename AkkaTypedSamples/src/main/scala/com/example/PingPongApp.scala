package com.example

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import akka.typed.ScalaDSL._
import akka.typed._

object PingPongApp extends App with Graphics {

  case class Ping(payload: String, replyTo: ActorRef[Pong])
  case class Pong(payload: String, replyTo: ActorRef[Ping])

  // Ping actor only responds to Pings
  // We need SelfAware so we can use the self as a reply ActorRef
  val pingBehaviour = SelfAware[Ping] { self =>
    Static { ping =>
      play
      Thread.sleep(300)
      ping.replyTo ! Pong(ping.payload, self)
    }
  }

  // Pong actor only responds to Pong
  val pongBehaviour = SelfAware[Pong] { self =>
    Static { pong =>
      playReverse
      Thread.sleep(300)
      pong.replyTo ! Ping(pong.payload, self)
    }
  }

  // Main behaviour for the app, creates the Ping and  Pong actors, and starts the flow
  val main: Behavior[akka.NotUsed] =
    Full {
      case Sig(ctx, PreStart) =>
        val pingActor = ctx.spawn(pingBehaviour, "PingActor")
        val pongActor = ctx.spawn(pongBehaviour, "PongActor")
        pongActor ! Pong("Hello!", pingActor)
        Same
    }

  // Start the game and stop after 10 seconds
  val system = ActorSystem("PingPongSystem", main)

  system.scheduler.scheduleOnce(10 seconds){
    system.terminate()
    println("GAME OVER")
  }

  Await.result(system.whenTerminated, 20 seconds)

}

trait Graphics {

  private def clear = print("\u001b[2J")

  def play = {
    clear
    println(
      """
       O                                O
      /    •                            |
      """
    )

  }

  def playReverse = {
    clear
    println(
      """
      O                                O
      |                           •     \
      """
    )
  }
}
