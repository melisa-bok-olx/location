package com.olx.location.services

import scala.util.Try

import org.bson.types.ObjectId

import com.mongodb.casbah.Imports.WriteConcern
import com.mongodb.casbah.MongoDB
import com.mongodb.casbah.WriteConcern
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.Context
import com.novus.salat.dao.SalatDAO
import com.olx.location.models.LocationTracks
import com.olx.location.models.Location
import com.olx.location.mongo.LocationPointModel
import com.olx.location.mongo.LocationTrackModel
import com.olx.location.mongo.LocationUserModel

import play.api.Play

trait MongoService {

  def saveLocationUser(locationUser: LocationUserModel): Try[Option[LocationUserModel]]
  def findLocationUser(email: String): Try[Option[LocationUserModel]]

  def saveLocationTrack(locationTrack: LocationTrackModel): Try[Option[LocationTrackModel]]
  def findLocationTracks(email: String, pageSize: Int, offset: Int): Try[List[LocationTrackModel]]
}

class MongoServiceImpl(mongoDatabase: MongoDB) extends MongoService {

  implicit val ctx = new Context {
    val name = "Custom_Classloader"
  }
  ctx.registerClassLoader(Play.classloader(Play.current))

  object LocationUserDAO extends SalatDAO[LocationUserModel, ObjectId](mongoDatabase("users"))
  object LocationTrackDAO extends SalatDAO[LocationTrackModel, ObjectId](mongoDatabase("tracks"))

  def saveLocationUser(locationUser: LocationUserModel): Try[Option[LocationUserModel]] = Try {

    LocationUserDAO.findOne(MongoDBObject("email" -> locationUser.email)) match {

      case Some(p) => {

        val toUpdate = p.copy(deviceId = locationUser.deviceId, lastLocation = locationUser.lastLocation)
        val result = LocationUserDAO.update(q = MongoDBObject("_id" -> p.id),
          toUpdate,
          upsert = false,
          multi = false,
          new WriteConcern)

        Some(toUpdate)
      }
      case None => {
        LocationUserDAO.insert(locationUser) match {
          case Some(id) => LocationUserDAO.findOneById(id)
          case _ => None
        }
      }
    }

  }

  def findLocationUser(email: String): Try[Option[LocationUserModel]] = Try {

    LocationUserDAO.findOne(MongoDBObject("email" -> email))

  }

  def saveLocationTrack(locationTrack: LocationTrackModel): Try[Option[LocationTrackModel]] = Try {

    LocationTrackDAO.insert(locationTrack) match {
      case Some(id) => LocationTrackDAO.findOneById(id)
      case None => None
    }
  }

  def findLocationTracks(email: String, pageSize: Int, offset: Int): Try[List[LocationTrackModel]] = Try {

    LocationTrackDAO.find(MongoDBObject("email" -> email))
    .skip(offset)
    .limit(pageSize)
    .toList

  }

}