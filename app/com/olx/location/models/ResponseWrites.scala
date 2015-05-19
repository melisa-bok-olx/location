package com.olx.location.models

import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Writes


trait ResponseWrites {

 
  implicit val locationUserWrites = new Writes[LocationUser] {
    
    def writes(locationUser: LocationUser): JsValue = {
      
      Json.obj(
    		  "email" -> locationUser.email ,
    		  "deviceId" -> locationUser.deviceId, 
    		  "latitude" -> locationUser.latitude,
    		  "longitude" -> locationUser.longitude 
      )
    }
  }  
  

}