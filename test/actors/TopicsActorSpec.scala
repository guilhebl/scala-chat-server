package actors

import akka.actor._
import akka.testkit.{ TestActorRef, _ }
import com.typesafe.config.ConfigFactory
import org.scalatest.MustMatchers
import play.api.libs.json._

import scala.concurrent.duration._

class TopicsActorSpec extends TestKitSpec with MustMatchers {

  "TopicsActor" should {

    val topicName = "ABC"

    "send a watchTopic when receiving a WatchTopic message" in {
      val userParentActor = TestProbe()
      val topicsActor = system.actorOf(Props(new TopicsActor(userParentActor.ref)))

      // create an actor which will test the TopicsActor
      val probe = new TestProbe(system)
      val userActor = system.actorOf(Props(new ProbeWrapper(probe)))

      // Simulates sending the message as if it was sent from the userActor
      topicsActor.tell(WatchTopic(topicName), userActor)

      // the userActor will be added as a watcher and get a message with the topic history
      val userActorMessage = probe.receiveOne(500.millis)
      userActorMessage mustBe a[TopicHistory]

      // user joined msg
      val msg2 = probe.receiveOne(500.millis)
      msg2 mustBe a[TopicUpdate]
    }

    "send a unwatchTopic when receiving a UnwatchTopic message" in {
      val userParentActor = TestProbe()
      val topicsActor = system.actorOf(Props(new TopicsActor(userParentActor.ref)))

      // create an actor which will test the TopicsActor
      val probe = new TestProbe(system)
      val userActor = system.actorOf(Props(new ProbeWrapper(probe)))

      // First watch topic
      topicsActor.tell(WatchTopic(topicName), userActor)

      // the userActor will be added as a watcher and get a message with the topic history
      val userActorMessage = probe.receiveOne(500.millis)
      userActorMessage mustBe a[TopicHistory]

      // user joined msg
      val msg2 = probe.receiveOne(500.millis)
      msg2 mustBe a[TopicUpdate]

      // now unwatch 
      topicsActor.tell(UnwatchTopic(Some(topicName)), userActor)

      // the userActorParent will be notified
      val msg = userParentActor.receiveOne(500.millis)
      msg mustBe a[TopicList]
    }

    "send a AddMessage when receiving an addMessage message" in {
      val userParentActor = TestProbe()
      val topicsActor = system.actorOf(Props(new TopicsActor(userParentActor.ref)))

      // create an actor which will test the TopicsActor
      val probe = new TestProbe(system)
      val userActor = system.actorOf(Props(new ProbeWrapper(probe)))

      // Simulates sending the message as if it was sent from the userActor
      topicsActor.tell(AddMessage("User1", topicName, "Hello World!"), userActor)

      // the userActor will be added as a watcher and get a message with the topic history
      val userActorMessage = probe.receiveOne(500.millis)
      userActorMessage mustBe a[TopicHistory]

      // user joined msg
      val msg2 = probe.receiveOne(500.millis)
      msg2 mustBe a[TopicUpdate]

      // user message added
      val msg3 = probe.receiveOne(500.millis)      
      msg3 mustBe a[TopicUpdate]
    }

    "send a fetchTopics when receiving a FetchTopics message" in {
      val userParentActor = TestProbe()
      val topicsActor = system.actorOf(Props(new TopicsActor(userParentActor.ref)))

      // create an actor which will test the TopicsActor
      val probe = new TestProbe(system)
      val userActor = system.actorOf(Props(new ProbeWrapper(probe)))

      // Simulates sending the message as if it was sent from the userActor
      topicsActor.tell(FetchTopics, userActor)

      // the userActor will be added as a watcher and get a message with the topic list
      val msg3 = probe.receiveOne(500.millis)
      msg3 mustBe a[TopicList]
    }

    "send a topicListUpdate when receiving a TopicListUpdate message" in {
      val userParentActor = TestProbe()
      val topicsActor = system.actorOf(Props(new TopicsActor(userParentActor.ref)))

      // create an actor which will test the TopicsActor
      val probe = new TestProbe(system)
      val topicActor = system.actorOf(Props(new ProbeWrapper(probe)))

      // Simulates sending the message as if it was sent from the topicActor
      topicsActor.tell(TopicListUpdate, topicActor)

      // the userActorParent will be notified
      val msg = userParentActor.receiveOne(500.millis)
      msg mustBe a[TopicList]
    }

  }
}
