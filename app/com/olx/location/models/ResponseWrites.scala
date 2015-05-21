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
  
  implicit val locationWrites = new Writes[Location] {
    
    def writes(location: Location): JsValue = {
      
      Json.obj(
    		  "datetime" -> location.datetime,
    		  "latitude" -> location.latitude, 
    		  "longitude" -> location.longitude
      )
    }
  }  
  
  implicit val locationTracksWrites = new Writes[LocationTracks] {
    
    def writes(locationTracks: LocationTracks): JsValue = {
      
      Json.obj(
    		  "email" -> locationTracks.email ,
    		  "locations" -> locationTracks.locations
      )
    }
  }  
  

}