package actors

import akka.actor._
import akka.testkit._

import scala.concurrent.duration._
import scala.collection.immutable.HashSet

class TopicActorSpec extends TestKitSpec {

  "TopicActor" should {
    val topicName = "ABC"

    "send TopicUpdate to all listeners on AddMessage" in {
      val topicActor = system.actorOf(Props(new TopicActor(topicName)))

      // create an actor which will test the TopicActor
      val probe = new TestProbe(system)
      val topicsActor = system.actorOf(Props(new ProbeWrapper(probe)))

      // Simulates sending the message as if it was sent from the topicsActor
      topicActor.tell(AddMessage("User1", topicName, "Hello World!"), topicsActor)

      // if user is new to the topic he will receive topic history
      val topicHistoryMessage = probe.receiveOne(500.millis)
      topicHistoryMessage mustBe a[TopicHistory]

      // user joined msg
      val msgAdded = probe.receiveOne(500.millis)
      msgAdded mustBe a[TopicUpdate]

      // finally the user intended message
      val userActorMessage = probe.receiveOne(500.millis)
      userActorMessage mustBe a[TopicUpdate]
    }

    "not send a TopicUpdate to all listeners on AddMessage if Msg is empty" in {
      val topicActor = system.actorOf(Props(new TopicActor(topicName)))

      // create an actor which will test the TopicActor
      val probe = new TestProbe(system)
      val topicsActor = system.actorOf(Props(new ProbeWrapper(probe)))

      // Simulates sending the message as if it was sent from the topicsActor
      topicActor.tell(AddMessage("User1", topicName, ""), topicsActor)

      // if user is new to the topic he will receive topic history
      val topicHistoryMessage = probe.receiveOne(500.millis)
      topicHistoryMessage mustBe a[TopicHistory]

      // user joined msg
      val msgAdded = probe.receiveOne(500.millis)
      msgAdded mustBe a[TopicUpdate]

      // finally expect no more msgs
      probe.expectNoMsg()
    }

    "add a watcher and send a TopicHistory message to the user" in {
      val probe = new TestProbe(system)

      // Create a standard TopicActor.
      val topicActor = system.actorOf(Props(new TopicActor(topicName)))

      // create an actor which will test the TopicActor
      val userActor = system.actorOf(Props(new ProbeWrapper(probe)))

      // Simulates sending the message as if it was sent from the userActor
      topicActor.tell(WatchTopic(topicName), userActor)

      // the userActor will be added as a watcher and get a message with the topic history
      val userActorMessage = probe.receiveOne(500.millis)
      userActorMessage mustBe a [TopicHistory]
    }

    "on unwatch if last watcher to unwatch should stop Actor" in {
      val probe = new TestProbe(system)
      val testProbe = TestProbe()
      
      // Create a standard TopicActor.
      val topicActor = system.actorOf(Props(new TopicActor(topicName)))
      testProbe watch topicActor
      
      // create an actor which will test the TopicActor
      val userActor = system.actorOf(Props(new ProbeWrapper(probe)))

      // Simulates sending the message as if it was sent from the userActor
      topicActor.tell(UnwatchTopic(Some(topicName)), userActor)

      // the topicActor is supposed to be killed when no more listeners are present
      testProbe.expectTerminated(topicActor)
    }

    "if not last watcher to unwatch should send topicupdate to all other listeners" in {
      val probe = new TestProbe(system)
      val testProbe = TestProbe()
      
      // Create a standard TopicActor.
      val topicActor = system.actorOf(Props(new TopicActor(topicName)))
      testProbe watch topicActor

      // start by adding 2 users to the topic
      val userActor = system.actorOf(Props(new ProbeWrapper(probe)))
      topicActor.tell(WatchTopic(topicName), userActor)
      val userActorMessage = probe.receiveOne(500.millis)
      userActorMessage mustBe a [TopicHistory]
      val userActorJoined = probe.receiveOne(500.millis)
      userActorJoined mustBe a [TopicUpdate]
      
      val userActor2 = system.actorOf(Props(new ProbeWrapper(probe)))
      topicActor.tell(WatchTopic(topicName), userActor2)
      val userActorMessage2 = probe.receiveOne(500.millis)
      userActorMessage2 mustBe a [TopicHistory]
      val userActorJoined2 = probe.receiveOne(500.millis)
      userActorJoined2 mustBe a [TopicUpdate]
            
      // Simulates sending the message as if it was sent from the userActor
      topicActor.tell(UnwatchTopic(Some(topicName)), userActor)

      // the topicActor supposed to send "user left" msg to all other remaining users of topic
      val msg = probe.receiveOne(500.millis)
      msg mustBe a [TopicUpdate]
    }

  }

}
