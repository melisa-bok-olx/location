package helpers

import play.api.mvc._
import scala.concurrent.Future
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import com.olx.location.driver.GraphiteClient
import play.api.libs.iteratee.Iteratee
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.util.Timeout

class ShopRequest[A](val key: Option[String], request: Request[A]) extends WrappedRequest[A](request)


case class Logging[A](key: String, action: Action[A])(implicit graphiteClient: GraphiteClient) extends Action[A] {

   def contentAsBytes(of: Future[Result]): Array[Byte] =
        Await.result(Await.result(of, Timeout(5 seconds).duration).body |>>> Iteratee.consume[Array[Byte]](), Timeout(5 seconds).duration)
  
  
  def apply(request: Request[A]): Future[Result] = {
    
    graphiteClient.addMetric(key)
    val startTime = System.currentTimeMillis()
    
    action(request).map { r =>
      
      val latency = System.currentTimeMillis() - startTime
      graphiteClient.addAverageMetric(key + ".latency" , latency.toInt)
      r match {
        case Result(header, body, connection) => {
          if (header.status != 200) {
            Logger.error("HTTP Error " + header.status + ": " + new String(contentAsBytes(Future.successful(r)), "utf-8"))
          }
          graphiteClient.addMetric(key + ".status." + header.status)
          r
        }
        case _ => r
      }
    }

  }

  lazy val parser = action.parser
}

