package com.aamir.akka.actors.infra


import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props, Terminated}
import akka.routing._
import com.typesafe.config.ConfigFactory

import java.util.UUID.randomUUID


/**
 * Routers are useful when we want to delegate or spread work in between multiple actors of the same kind
 * Routers are actually mid-level actors that forward messages to other actors (routees), either created by routers by themselves or outside.
 * Supported option for routing logic: round-robin, random, smallest mailbox, broadcast, tail-chopping, scatter-gather-first, consistent hashing.
 * Round-Robin(cycles between routees)
 * smallest mailbox(as a load balancing heuristic , always send next message to actor with fewest messages in queue),
 * BroadCast(redundancy measure, same messages to all routees),
 * ScatterGatherFirst(broadcasts i.e, sends same messages to all routees, take first reply, ignores rest),
 * tail-chopping(which forwards next message to each actor sequentially until first reply is received and other replies are discarded)
 * and consistent hashing(message with same hash get to the same actor)
 **/

object Routers1 extends App {

  /**
   * #1 - manual router
   */
  case class SyncResult(x: String)
  class Master extends Actor {

    private val slaves = for (i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave], s"slave-$i")
      ActorRefRoutee(slave)
    }
    private var router = Router(RoundRobinRoutingLogic(), slaves)

    override def receive: Receive = {
      case message         => router.route(message, sender())
      case message         => router.route(message, sender())
      case Terminated(ref) => {
        router = router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        router = router.addRoutee(ActorRefRoutee(newSlave))
      }
      case SyncResult(x) => {
        println("came here too")
        println(x)
      }
    }
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message =>
        if(message == "[9]-Hello Router") {
          println("came here")
          println(sender.path.name)
          sender() ! SyncResult("sync result")
        } else println(message.toString)
    }
  }


  val system = ActorSystem("RoutersDemo")
  val master = system.actorOf(Props[Master], "Master_Actor")

  for (i <- 1 to 10) {
    Thread.sleep(1000)
    master ! s"[$i]-Hello Router"
  }
}

object Router2 extends App {

  /**
   * Method #2 - a router actor with its own children
   * POOL router, Pool means router actor with its own children
   * Programmatically
   */

  class Slave extends Actor with ActorLogging {

    override def preStart(): Unit = {
      super.preStart()
      println("prestart===")
    }
    override def receive: Receive = {
      case message => println(s"${self.path.toString}: ${message.toString}")
    }
  }


  val system = ActorSystem("RoutersDemo")
  val master: ActorRef = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "simpleMaster")

  println(master.path)
  println(master.path.name)
  master ! "abc"
/*  for (i <- 1 to 10) {
    master ! s"[$i]-Hello Router"
  }*/
}

class SlaveRuntime extends Actor with ActorLogging {

  override def preStart(): Unit = {
    super.preStart()
    println("prestart===")
  }
  override def receive: Receive = {
    case message => println(s"${self.path.toString}: ${message.toString}")
  }
}
object RouterRuntime extends App {
  val system = ActorSystem("RoutersDemo")
  val fullyQualifiedWorkerName = "com.aamir.akka.actors.infra.SlaveRuntime"
  val props = Props.apply(Class.forName(fullyQualifiedWorkerName).asInstanceOf[Class[Actor]])

  val master: ActorRef = system.actorOf(RoundRobinPool(5).props(props),randomUUID().toString)

  for (i <- 1 to 10) {
      master ! s"[$i]-Hello Router"
    }
}

object Router3 extends App {

  /**
   * From Configurations
   */

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => {
        println(message.toString)
        context.stop(self)
        context.system.terminate()
      }
    }
  }


  val system = ActorSystem("RoutersDemo", ConfigFactory.load().getConfig("routersDemo"))
  val master: ActorRef = system.actorOf(FromConfig.props(Props[Slave]), "poolMaster")

  println(master.path)

    master ! s"[s]-Hello Router"
     master ! "Abc"
}

object Router4 extends App {

  /**
   * router with actors created elsewhere
   * Group Router
   * Programmatically
   */

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("RoutersDemo")

  // .. in another part of my application
  val slaveList = (1 to 5).map(i => system.actorOf(Props[Slave], s"$i-slave")).toList
  val slavePaths: List[String] = slaveList.map(_.path.toString)
  val groupMaster = system.actorOf(RoundRobinGroup(slavePaths).props())

  for (i <- 1 to 10) {
    groupMaster ! s"[$i]-Hello Router"
  }
}

object Router5 extends App {

  /**
   * router with actors created elsewhere
   * Group Router
   * Programmatically
   */

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }
  val system = ActorSystem("RoutersDemo", ConfigFactory.load().getConfig("routersDemo"))

  // .. in another part of my application
  val slaveList = (1 to 5).map(i => system.actorOf(Props[Slave], s"$i-slave")).toList
  val slavePaths: List[String] = slaveList.map(_.path.toString)


  val groupMaster = system.actorOf(FromConfig.props(), "groupMaster")

  for (i <- 1 to 10) {
    groupMaster ! s"[$i]-Hello Router"
  }
}


object Router6 extends App {

  /**
   * router with actors created elsewhere
   * Group Router
   * Programmatically
   */

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }
  val system = ActorSystem("RoutersDemo", ConfigFactory.load().getConfig("routersDemo"))

  // .. in another part of my application
  val slaveList = (1 to 5).map(i => system.actorOf(Props[Slave], s"$i-slave")).toList
  val slavePaths = slaveList.map(_.path.toString)


  val groupMaster = system.actorOf(FromConfig.props(), "groupMaster")

  /**
   * Special messages
   */
  groupMaster ! Broadcast("hello, everyone")
  //PoisonPill, Kill are not routed
}