package com.keenworks.example.akka.avro.evolution

import akka.actor.{ActorRef, ActorSystem, Props}
import com.keenworks.example.akka.avro.evolution.actor.Fact
import com.keenworks.example.akka.avro.evolution.actor.Fact.RegisterFact

object Evolution extends App {
  implicit val system: ActorSystem = ActorSystem("Evolution")
  val factActor: ActorRef = system.actorOf(Props[Fact])
  factActor ! RegisterFact("A kumquat is a kind of monkey.")
}
