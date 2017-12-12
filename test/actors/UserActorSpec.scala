package actors

import akka.actor._
import akka.testkit.{TestActorRef, _}
import com.typesafe.config.ConfigFactory
import org.scalatest.MustMatchers
import play.api.libs.json._

import scala.concurrent.duration._

class UserActorSpec extends TestKitSpec with MustMatchers {

  "UserActor" should {

    val topicName = "ABC"
    val scores = scala.collection.immutable.Seq[(String,Int)](("User1", 1450), ("User2", 2152), ("User3", 810))
    val history = scala.collection.immutable.Seq[String]("User1 - hello there!", "User 2 - next string")
    val topics = scala.collection.immutable.Seq[String]("Adventure", "Boxing", "Cars", "MAIN", "Programming")
    val configuration = play.api.Configuration.apply(ConfigFactory.parseString(
      """
        |default.topics = ["MAIN"]
      """.stripMargin))

    "send a topicUpdate when receiving a TopicUpdate message" in {
      val out = TestProbe()
      val topicsActor = TestProbe()

      val userActorRef = TestActorRef[UserActor](Props(new UserActor(out.ref, topicsActor.ref, configuration)))
      val userActor = userActorRef.underlyingActor

      // send off the topic update...
      val msg = "User3 - Hello World"
      userActor.receive(TopicUpdate(topicName, msg, scores))

      val jsObj: JsObject = out.receiveOne(500 millis).asInstanceOf[JsObject]
      jsObj \ "type" mustBe JsDefined(JsString("topicupdate"))
      jsObj \ "topic" mustBe JsDefined(JsString(topicName))
      jsObj \ "msg" mustBe JsDefined(JsString(msg))      
      jsObj \ "scores" mustBe JsDefined(Json.arr(
              Json.arr(JsString("User1"), JsNumber(1450)),
              Json.arr(JsString("User2"), JsNumber(2152)),
              Json.arr(JsString("User3"), JsNumber(810))))            
    }

    "send the topic history when receiving a TopicHistory message" in {
      val out = TestProbe()
      val topicsActor = TestProbe()

      val userActorRef = TestActorRef[UserActor](Props(new UserActor(out.ref, topicsActor.ref, configuration)))
      val userActor = userActorRef.underlyingActor

      // send off the topic update...
      userActor.receive(TopicHistory(topicName, history))
      val jsObj: JsObject = out.receiveOne(500 millis).asInstanceOf[JsObject]

      // ...and expect it to be a JSON node.
      jsObj \ "type" mustBe JsDefined(JsString("topichistory"))
      jsObj \ "topic" mustBe JsDefined(JsString(topicName))
      jsObj \ "history" mustBe JsDefined(Json.arr(JsString("User1 - hello there!"), JsString("User 2 - next string")))
      jsObj \ "user" mustBe JsDefined(JsString("$$b"))          
    }
    
    "send the topic list when receiving a TopicList message" in {
      val out = TestProbe()
      val topicsActor = TestProbe()

      val userActorRef = TestActorRef[UserActor](Props(new UserActor(out.ref, topicsActor.ref, configuration)))
      val userActor = userActorRef.underlyingActor

      // send off the topic list...
      userActor.receive(TopicList(topics))
      val jsObj: JsObject = out.receiveOne(500 millis).asInstanceOf[JsObject]

      // ...and expect it to be a JSON node.
      // ("Adventure", "Boxing", "Cars", "MAIN", "Programming")
      jsObj \ "type" mustBe JsDefined(JsString("topiclist"))
      jsObj \ "topics" mustBe JsDefined(Json.arr(
          JsString("Adventure"), 
          JsString("Boxing"),
          JsString("Cars"),
          JsString("MAIN"),
          JsString("Programming")))          
    }

    "send addMessage to topics when chat message type received" in {
      val out = TestProbe()
      val topicsActor = TestProbe()

      val userActorRef = TestActorRef[UserActor](Props(new UserActor(out.ref, topicsActor.ref, configuration)))
      val userActor = userActorRef.underlyingActor

      // send off the topic JSON...
      val jsonString = """{"msgType": "chat", "topic": "ABC", "msg": "Hello World!"}"""
      val json = Json.parse(jsonString)
      
      userActor.receive(json)
      
      // the topicsActor will receive an addMessage and 2 extra msgs
      val msg1 = topicsActor.receiveOne(500.millis)
      val msg2 = topicsActor.receiveOne(500.millis)
      val addMessage = topicsActor.receiveOne(500.millis)
      
      msg1 mustBe a [WatchTopic]
      msg2 mustBe FetchTopics
      addMessage mustBe a [AddMessage]      
    }

    "send unwatch to topics when unwatch message type received" in {
      val out = TestProbe()
      val topicsActor = TestProbe()

      val userActorRef = TestActorRef[UserActor](Props(new UserActor(out.ref, topicsActor.ref, configuration)))
      val userActor = userActorRef.underlyingActor
     
      // 1. Watch topic
      val jsonString = """{"msgType": "chat", "topic": "ABC", "msg": ""}"""
      val json = Json.parse(jsonString)      
      userActor.receive(json)
      val msg1 = topicsActor.receiveOne(500.millis)
      val msg2 = topicsActor.receiveOne(500.millis)
      val addMessage = topicsActor.receiveOne(500.millis)
      msg1 mustBe a [WatchTopic]
      msg2 mustBe FetchTopics      
      addMessage mustBe a [AddMessage]      
      
      // 2. UnWatch topic
      val jsonString2 = """{"msgType": "unwatch", "topic": "ABC", "msg": ""}"""
      val json2 = Json.parse(jsonString2)      
      userActor.receive(json2)      
      val unwatchMsg = topicsActor.receiveOne(500.millis)      
      unwatchMsg mustBe a [UnwatchTopic]
    }

  }  
}
