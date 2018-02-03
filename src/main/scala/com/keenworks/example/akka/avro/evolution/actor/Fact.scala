package com.keenworks.example.akka.avro.evolution.actor

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.keenworks.example.akka.avro.evolution.actor.Fact.{FactHistory, GetFactHistory, RegisterFact, Statement}

class Fact extends PersistentActor with ActorLogging {
  override def persistenceId: String = "example-fact"

  override def receiveRecover: Receive = {
    case evt: Statement => updateFactHistory(evt)
    case RecoveryCompleted => log.info("Recovery completed!")
  }

  override def receiveCommand: Receive = {
    case RegisterFact(statement) =>
      log.info("Got a RegisterFact command")
      persist(Statement(statement))(updateFactHistory)
    case GetFactHistory =>
      log.info("Got a GetFactHistory command, sending event size of: " + factHistory.size)
      sender() ! factHistory.events
  }

  var factHistory = FactHistory()
  def updateFactHistory(event: Statement): Unit = {
    factHistory = factHistory.updated(event)
  }
}

object Fact {
  sealed trait FactCommand
  final case class RegisterFact(statement: String) extends FactCommand

  final case class Statement(statement: String)

  final case class FactHistory(events: List[Statement] = Nil) {
    def updated(evt: Statement): FactHistory = copy(evt :: events)
    def size: Int = events.length
  }

  // TODO: Testing only - try to mixin as a testing trait instead
  case object GetFactHistory
}