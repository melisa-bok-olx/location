package com.olx.location.models

import com.olx.location.mongo.LocationUserModel
import com.olx.location.mongo.LocationPointModel



case class LocationUser(email: String, deviceId: String, latitude: Double, longitude: Double) {
  
  
  def toLocationUserModel(): LocationUserModel = {
    
    val location = List(longitude , latitude)
    LocationUserModel(email = email, deviceId = deviceId, lastLocation = LocationPointModel(coordinates = location))
    
  }
}