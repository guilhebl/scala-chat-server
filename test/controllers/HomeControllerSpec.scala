package controllers

import actors.TestKitSpec
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.TestProbe
import org.scalatest.MustMatchers
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext

class HomeControllerSpec extends TestKitSpec with MustMatchers {

  "createWebSocketFlow" should {

    "create a websocket flow and send a message through" in {
      implicit val materializer = ActorMaterializer()(system)
      implicit val ec: ExecutionContext = system.dispatcher

      val topicsActor = TestProbe("topicsActor")
      val userParentActor = TestProbe("userParentActor")
      val userActor = TestProbe("userActor")
      val publisher = akka.stream.testkit.TestPublisher.probe[JsValue]()

      // instantiate the controller...
      val cc = play.api.test.Helpers.stubControllerComponents()
      val controller = new HomeController(topicsActor.ref, userParentActor.ref, cc)

      // call method under test...
      val flowUnderTest = controller.createWebSocketFlow(publisher, userActor.ref)

      // create a test source and sink around the flow
      val (pub, sub) = TestSource.probe[JsValue]
        .via(flowUnderTest)
        .toMat(TestSink.probe[JsValue])(Keep.both)
        .run()

      val jsvalue = Json.obj("foo" -> "bar")

      // check that a message sent in will come out the other end
      sub.request(n = 1)
      publisher.sendNext(jsvalue)
      sub.expectNext(jsvalue)
    }

  }


}
