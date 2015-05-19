package com.olx.location.controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.olx.location.models.LocationUser
import com.olx.location.models.RequestReads
import com.olx.location.services.LocationService

import play.api.libs.json.JsError
import play.api.libs.json.Json
import play.api.mvc._

object LocationController extends Controller with LocationController {

  val locationService = LocationService()
}

trait LocationController extends RequestReads {

  this: Controller =>
  val locationService: LocationService

  
  def health = Action { request =>
    Ok("")
  }
  
  def registerUser = Action.async(parse.json) { request =>

    val locationUser = request.body.validate[LocationUser]

    locationUser.fold(
      errors => {
        Future.successful(BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors))))
      },
      user => {
        locationService.registerUser(user).map { response =>
          response match {
            case Right(r) => Ok(Json.toJson("123456"))
            case Left(e: Exception) => InternalServerError(Json.obj("status" -> "KO", "message" -> e.getMessage()))
          }
        }
      })

  }

}