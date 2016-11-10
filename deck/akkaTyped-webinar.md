autoscale: true
build-lists: true
theme: Merriweather, 9

![](images/working.jpg)

![inline 50%](images/yp.logo.white.png)

# Akka Typed

## Type sanity for your actors

### Alejandro Lujan

---

![](images/working.jpg)

# Who Am I?

- Typesafe => Lightbend fan for ~6 years
  - Developer, Trainer, Mentor
  - Scala, Akka, Play, *Lagom*
- ![inline 35%](images/sun.png) Scala Up North organizer 
- Java dev / architect for 10 years
- Other stuff: C#, C++, Even PHP

---

![](images/working.jpg)

# What we'll cover

- The problem with *untyped* actors
- The basics of Akka Typed
- Code samples
- Limitations

---

# The problem with untyped actors

```scala
// Find the bug...
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
```

---

# The problem with untyped actors

```scala
// Answer: This is a case class...
case class Envelope(content: String)

class MyActor extends Actor {
  override def receive: Receive = {
  
    // And we're matching on the companion object! :(
    case Envelope =>
      println("Received an envelope!")
  }
}

```

---

# The problem with untyped actors

- By default unhandled messages are posted to the Event Stream
- Are you monitoring the Event Stream?
- Alternatively, log on `case _`

---

# [fit] Why not use the compiler?

---

# The goal

- Create actors that expose the type of messages they can handle
- Enforce at compile time

---

# Basics of Akka Typed

- **`ActorRef[M]`**
  An actor reference that can only receive messages of type M

- **`ActorSystem[M]`**
  An actor system with a **root** actor that receives messages of type M
  
- **`Behavior[M]`**
  DSL constructor of an actor that reacts to messages of type M

---

# Behaviors

Defined in `akka.typed.ScalaDSL` :

- **Static** for simple actors that don't change behaviors
- **ContextAware** to access the actor `context`
- **SelfAware** to access the `self`
- **Total** for an actor defined by a total function

---

# The simplest example

Our *Echo* actor replies with the same payload we send

```scala
// Incoming and outgoing message types
case class Message(payload: String, replyTo: ActorRef[Echo])
case class Echo(payload: String)

// No sender() method, so we need the reply address in the message
val echo = Static[Message] { message =>
  println(s"Received this: '$message'")
  
  // Cannot send anything else but Echo through this ! operator
  message.replyTo ! Echo(message.payload)
}
```

---

# The simplest example

```scala

val echo = Static[Message] { ... }

// The actor system we'll use to host the Echo actor
// The echo behavior is the root actor!
val system: ActorSystem[Message] = ActorSystem("EchoSystem", echo)

// Note the Future is already typed
val future: Future[Echo] = system ? (Message("Hola", _))
```

---

# [fit] Slightly more complicated - PONG!

```scala
case class Ping(payload: String, replyTo: ActorRef[Pong])
case class Pong(payload: String, replyTo: ActorRef[Ping])

// Ping actor only responds to Pings
val pingBehaviour = SelfAware[Ping] { self =>
  Static { ping =>
    ping.replyTo ! Pong(ping.payload, self)
  }
}

// Pong actor only responds to Pong
val pongBehaviour = SelfAware[Pong] { self =>
  Static { pong =>
    pong.replyTo ! Ping(pong.payload, self)
  }
}
```

---

# [fit] Slightly more complicated - PONG!

```scala

val pingBehaviour: Behavior[Ping] = ...
val pongBehaviour: Behavior[Pong] = ...

// Main behaviour for the app, creates the Ping and  Pong actors, and starts the flow
val main: Behavior[akka.NotUsed] =
  Full {
    case Sig(ctx, PreStart) =>
      val pingActor = ctx.spawn(pingBehaviour, "PingActor")
      val pongActor = ctx.spawn(pongBehaviour, "PongActor")
      pongActor ! Pong("Hello!", pingActor)
      Same
  }

// Start the game
val system = ActorSystem("PingPongSystem", main)
```

---

# Limitations

TODO

- Changing state through become 
- Actor distribution?
- Routers?
- Clustering?
- Documentation is sparse

---

![](images/working.jpg)

# That's all folks!

<br>

github.com/alejandrolujan/AkkaTyped

<br>

doc.akka.io/docs/akka/2.4/scala/typed.html

---

![](images/working.jpg)

# [fit] Questions?

---

![](images/working.jpg)

# [fit] Thanks!

## @Yopp_Works
## yoppworks.com/events
## info@yoppworks.com