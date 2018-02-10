package com.keenworks.example.akka.avro.evolution.actor

import akka.actor.{ActorSystem, Kill, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.keenworks.example.akka.avro.evolution.actor.Fact.{GetFactHistory, RegisterFact, Statement}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class FactPersistenceSpec
  extends TestKit(ActorSystem("FactPersistenceSpec"))
    with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "FactActor" should {
    val statement = Statement("A bird is a mammal", TruthMatrix(5.2, 4.5), truth=false)

    "add a FactEvent to the history and preserve history after restart" in {
      val factActor = system.actorOf(Props(new Fact), "factActor")
      factActor ! RegisterFact("A bird is a mammal")
      factActor ! GetFactHistory // TODO: supply this message-handler as a mixin trait instead
      expectMsg(List(statement))

      factActor ! Kill    // TODO: supply alternate supervision strategy as a mixin??

      factActor ! GetFactHistory
      expectMsg(List(statement))
    }

  }

}
