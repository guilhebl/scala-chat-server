package actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.LoggingReceive
import java.util.Calendar

import scala.collection.immutable.{HashSet}
import scala.concurrent.duration._

import utils.ScoreServiceWrapper.score
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer


/**
 * There is one TopicActor per topic.  The TopicActor maintains a list of users watching the topic and the topic
 * values.  Each TopicActor updates all their listeners once a new msg arrives
 */
class TopicActor(topic: String) extends Actor with ActorLogging {

  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]
  
  // chat history and score list are mutable in-memory data models
  val chatHistory: ListBuffer[String] = ListBuffer("")  
  val chatScores: HashMap[String,Int] = HashMap();  
  
  override def preStart(): Unit = {
    super.preStart()
    context.parent ! TopicListUpdate
  }

  override def postStop(): Unit = {
    context.parent ! TopicListUpdate
  }
  
  private def addUser(u:ActorRef) = {
      // add the watcher to the list
      watchers = watchers + u
      
      // update chat user joined
      chatHistory += u.path.name + " joined"      
      watchers.foreach(_ ! TopicUpdate(topic, chatHistory.last, getTopTen))    
  }
  
  private def getTopTen = {
    chatScores.view.toList.sortBy(- _._2).take(10)
  }
  
  def receive = LoggingReceive {
    case AddMessage(user, topic, msg) =>      
      // add watcher if not already in topic listeners
      if (!watchers.contains(sender)) {
        sender ! TopicHistory(topic, chatHistory.toList)
        addUser(sender)
      }
      
      // add a new msg to topic
      if (!msg.equals("")) {
        chatHistory += sender.path.name + ": " + msg      
        val msgScore = score(msg).getOrElse(0)      
        chatScores.get(user) match {
          case None => chatScores(user) = msgScore
          case Some(v) => chatScores(user) += msgScore
        }      
        // notify watchers
        watchers.foreach(_ ! TopicUpdate(topic, chatHistory.last, getTopTen))        
      }
      
    case WatchTopic(topic) =>
      // send the topic history to the user
      sender ! TopicHistory(topic, chatHistory.toList)
      addUser(sender)
      
    case UnwatchTopic(topic) =>
      watchers = watchers - sender
      if (watchers.isEmpty) {        
        context.stop(self)        
      } else if (!topic.isEmpty) {
        // update chat user left
        chatHistory += sender.path.name + " left"
        chatScores.remove(sender.path.name)
        watchers.foreach(_ ! TopicUpdate(topic.get, chatHistory.last, getTopTen))           
      }
  }
}

case object FetchTopics

case object TopicListUpdate

case class AddMessage(user: String, topic: String, newmsg: String)

case class TopicUpdate(topic: String, msg: String, scores: scala.collection.immutable.Seq[(String,Int)])

case class TopicHistory(topic: String, history: scala.collection.immutable.Seq[String])

case class TopicList(topics: scala.collection.immutable.Seq[String])

case class WatchTopic(topic: String)

case class UnwatchTopic(topic: Option[String])
