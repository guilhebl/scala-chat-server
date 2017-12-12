package utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import scala.concurrent._
import scala.concurrent.duration._

import scorer.ScorerService
import akka.util.Timeout

/**
 * Score Wrapper object responsible for handling calls to external Score API,
 * since chat is a fluid non-stop conversation we need to keep the chat going and app running 
 * in case a call to score service hangs. In order to avoid having the user stuck or waiting 
 * we establish a 5 second Timeout. (in a production env. this timeout value could be placed in a config or properties file,
 * as well as an optional retry mechanism could be configured in order to consolidate "pending" message scores.)
 */
trait ScoreWrapper {
  def score(msg:String): Option[Int]
}

object ScoreServiceWrapper extends ScoreWrapper {

  lazy val scorerService = new ScorerService()
  
  def score(msg:String): Option[Int] = {
    val f = Future { Some(scorerService.getScorer().score(msg)) }
    
    try {
      Await.result(f, 5 seconds)
    } catch {
      case e: TimeoutException => Some(0)
    }

  }

}
