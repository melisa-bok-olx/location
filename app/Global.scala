import scala.concurrent.Future

import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.ExecutionContext.Implicits.global

object Global extends GlobalSettings {

  override def onError(request: RequestHeader, ex: Throwable) = {
    Logger.error("Global error", ex)
    Future.successful(InternalServerError(Json.obj("status" -> "KO", "message" -> ex.getMessage())))
  }
  
 /**
   * Global action composition.
   */
  override def doFilter(action: EssentialAction): EssentialAction = EssentialAction { request =>
    action.apply(request).map(_.withHeaders(
      "Access-Control-Allow-Origin" -> "*"
    ))
  }  
    
}