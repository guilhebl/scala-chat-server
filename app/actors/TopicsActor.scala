package actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import java.util.Calendar
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import javax.inject.Inject
import javax.inject.Named
import play.api.libs.concurrent.InjectedActorSupport

import akka.event.LoggingReceive

class TopicsActor @Inject()(@Named("userParentActor") userParentActor: ActorRef) 
  extends Actor with ActorLogging {

  def receive = LoggingReceive {
    case watchTopic@WatchTopic(topic) =>
      // get or create the TopicActor for the topic and forward this message
      context.child(topic).getOrElse {
        context.actorOf(Props(new TopicActor(topic)), topic)        
      } forward watchTopic
      
    case addMessage@AddMessage(user, topic, msg) =>
      // get TopicActor for the topic and forward this message
      context.child(topic).getOrElse {
        context.actorOf(Props(new TopicActor(topic)), topic)        
      } forward addMessage
      
    case fetchTopics@FetchTopics =>
      val topics = context.children.map(_.path.name).toList.sorted
      sender ! TopicList(topics)   

    case topicListUpdate@TopicListUpdate =>
      val topics = context.children.map(_.path.name).toList.sorted
      userParentActor ! TopicList(topics)   

    case unwatchTopic@UnwatchTopic(Some(topic)) =>
      // if there is a TopicActor for the topic forward this message
      context.child(topic).foreach(_.forward(unwatchTopic))
    case unwatchTopic@UnwatchTopic(None) =>
      // if no topic is specified, forward to everyone
      context.children.foreach(_.forward(unwatchTopic))
  }

}
