package actors

import java.util.Calendar

import com.google.inject.assistedinject.Assisted

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import javax.inject.Inject
import javax.inject.Named
import play.api.Configuration
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.json.JsValue
import play.api.libs.json.Json

class UserActor @Inject()(@Assisted out: ActorRef,
                          @Named("topicsActor") topicsActor: ActorRef,
                          configuration: Configuration) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    super.preStart()

    configureDefaultTopics()
  }

  def configureDefaultTopics(): Unit = {
    import scala.collection.JavaConverters._
    val defaultTopics = configuration.getStringList("default.topics").get.asScala
        
    log.info(s"Creating user actor with default topics $defaultTopics")

    // start by watching main topic
    for (topic <- defaultTopics) {
      topicsActor ! WatchTopic(topic)
    }    
    
    // fetch all existing topics
    topicsActor ! FetchTopics
  }

  override def receive: Receive = LoggingReceive {
    case TopicUpdate(topic, msg, scores) =>
      val jsonOutput = Json.obj(
          "type" -> "topicupdate", 
          "topic" -> topic, 
          "msg" -> msg,
          "scores" -> scores
      )
      out ! jsonOutput

    case TopicHistory(topic, history) =>
      val msgSeq = history.map(h => Json.toJson[String](h))
      val jsonOutput = Json.obj(
          "type" -> "topichistory", 
          "user" -> self.path.name, 
          "topic" -> topic, 
          "history" -> msgSeq
      )
      out ! jsonOutput

     case TopicList(topics) =>
      val topicSeq = topics.map(h => Json.toJson[String](h))
      val jsonOutput = Json.obj(
          "type" -> "topiclist", 
          "topics" -> topicSeq
      )
      out ! jsonOutput

    case json: JsValue =>
      val topic = (json \ "topic").as[String]
      val msg = (json \ "msg").as[String]
      val msgType = (json \ "msgType").as[String]
      
      msgType match {
        case "chat" => topicsActor ! AddMessage(self.path.name, topic, msg)
        case "unwatch" => topicsActor ! UnwatchTopic(Some(topic))
      }                  
  }
}

class UserParentActor @Inject()(childFactory: UserActor.Factory) extends Actor with InjectedActorSupport with ActorLogging {
  import UserParentActor._

  override def receive: Receive = LoggingReceive {
    case Create(id, out) =>      
      val child: ActorRef = injectedChild(childFactory(out), s"userActor-$id")
      sender() ! child
      
    case topicList@TopicList(topics) =>      
      context.children.foreach(_.forward(topicList))
  }
}

object UserParentActor {
  case class Create(id: String, out: ActorRef)
}

object UserActor {
  trait Factory {
    // Corresponds to the @Assisted parameters defined in the constructor
    def apply(out: ActorRef): Actor
  }
}
