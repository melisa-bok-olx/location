package com.olx.location.models

import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import play.api.data.validation.ValidationError

trait RequestReads {

  
  implicit val locationUserRequestReads = new Reads[LocationUser] {
    
    def reads(json: JsValue): JsResult[LocationUser] = {
      
      val email: Option[String] = (json \ "email").asOpt[String]
      val deviceId: Option[String] = (json \ "deviceId").asOpt[String]
      val latitude: Float = (json \ "latitude").asOpt[Float].getOrElse(0)      
      val longitude: Float = (json \ "longitude").asOpt[Float].getOrElse(0)
      
      (email, deviceId) match {
        case (Some(e), Some(r)) => JsSuccess(LocationUser(e, r, latitude, longitude))
        case _ => JsError("Unexpected JSON for a new LocationUser: " + json)
      }
    }
  }
}