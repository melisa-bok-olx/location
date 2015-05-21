package com.olx.location.mongo

import org.bson.types.ObjectId
import com.novus.salat.annotations._


case class LocationPointModel(@Key("type")_type: String = "Point", coordinates: List[Double])

case class LocationUserModel(@Key("_id")id: ObjectId = new ObjectId, 
    email: String, 
    deviceId: String, 
    lastLocation: LocationPointModel)
    
case class LocationTrackModel(@Key("_id")id: ObjectId = new ObjectId, email: String, location: LocationPointModel)