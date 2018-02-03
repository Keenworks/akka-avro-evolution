package com.keenworks.example.akka.avro.evolution.util

import akka.actor.{ActorInitializationException, ActorKilledException, DeathPactException, SupervisorStrategyConfigurator}
import com.typesafe.scalalogging.StrictLogging

class TestSupervisionStrategy extends SupervisorStrategyConfigurator with StrictLogging {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  import scala.concurrent.duration._

  override def create(): OneForOneStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
    case _: ActorInitializationException => Stop
    case _: ActorKilledException =>
      logger.info("Actor Killed... restarting")
      Restart  // Redefined so Kill will Restart rather than Stop, just for test purposes
    case _: DeathPactException => Stop
    case _: Exception => Restart
    case _ => Escalate
  }

}