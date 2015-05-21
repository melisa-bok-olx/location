package com.olx.location.controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.olx.location.models.LocationUser
import com.olx.location.models.RequestReads
import com.olx.location.services.LocationService
import play.api.libs.json.JsError
import play.api.libs.json.Json
import play.api.mvc._
import com.olx.location.models.ResponseWrites

object LocationController extends Controller with LocationController {

  val locationService = LocationService()
}

trait LocationController extends RequestReads with ResponseWrites {

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
            case Right(r) => Ok(Json.toJson(r))
            case Left(e: Exception) => InternalServerError(Json.obj("status" -> "KO", "message" -> e.getMessage()))
          }
        }
      })

  }

  def updateLocation(email: String, latitude: Double, longitude: Double) = Action.async { request =>

    locationService.updateUserLocation(email, latitude, longitude).map { response =>
      response match {
        case Right(r) => Ok(Json.toJson(r))
        case Left(e: Exception) => InternalServerError(Json.obj("status" -> "KO", "message" -> e.getMessage()))
      }
    }

  }
  
  def getUser(email: String) = Action.async { request =>
    
    locationService.getUser(email).map { response =>
      response match {
        case Right(r) => Ok(Json.toJson(r))
        case Left(e: Exception) => InternalServerError(Json.obj("status" -> "KO", "message" -> e.getMessage()))
      }
    }
    
  }
  
  def getUserLocations(email: String, pageSize: Int, offset: Int) = Action.async { request =>
    
    locationService.getUserLocations(email, pageSize, offset).map { response =>
      response match {
        case Right(r) => Ok(Json.toJson(r))
        case Left(e: Exception) => InternalServerError(Json.obj("status" -> "KO", "message" -> e.getMessage()))
      }
    }
    
  }  
  

}