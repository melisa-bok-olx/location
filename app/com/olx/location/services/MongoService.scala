package com.olx.location.services

import com.olx.location.mongo.LocationUserModel
import scala.util.Try
import org.bson.types.ObjectId
import com.mongodb.casbah.MongoDB
import com.mongodb.casbah.Imports.WriteConcern
import com.novus.salat.dao.SalatDAO
import com.novus.salat.Context
import play.api.Play
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.WriteConcern
import com.olx.location.mongo.LocationPointModel

trait MongoService {

  def saveLocationUser(locationUser: LocationUserModel): Try[Option[ObjectId]]
}

class MongoServiceImpl(mongoDatabase: MongoDB) extends MongoService {

  implicit val ctx = new Context {
    val name = "Custom_Classloader"
  }
  ctx.registerClassLoader(Play.classloader(Play.current))

  object LocationUserDAO extends SalatDAO[LocationUserModel, ObjectId](mongoDatabase("users"))

  def saveLocationUser(locationUser: LocationUserModel): Try[Option[ObjectId]] = Try {

    LocationUserDAO.findOne(MongoDBObject("email" -> locationUser.email)) match {

      case Some(p) => {
        
        val location = MongoDBObject("type" -> "Point", "coordinates" -> locationUser.lastLocation.coordinates)
        
        val toUpdate = MongoDBObject("email" -> locationUser.email, 
            "deviceId" -> locationUser.deviceId, 
            "lastLocation" -> location)
        val result = LocationUserDAO.update(q = MongoDBObject("_id" -> p.id),
          toUpdate,
          upsert = false,
          multi = false)

        Some(p.id)
      }
      case None => LocationUserDAO.insert(locationUser)
    }

  }

}