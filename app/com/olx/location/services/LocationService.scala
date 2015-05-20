package com.olx.location.services

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import com.olx.location.models.LocationUser
import play.api.Logger
import com.mongodb.casbah.MongoClientOptions
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.ReadPreference
import play.api.libs.json.Json
import com.mongodb.ServerAddress
import com.olx.location.mongo.LocationPointModel
import com.olx.location.mongo.LocationUserModel

object LocationService {

  def apply(): LocationService = {

    val appPath = play.api.Play.current.path.getPath()
    val mongoConfiguration = Json.parse(scala.io.Source.fromFile(appPath + "/config/mongo.json").mkString)

    val servers = (mongoConfiguration \ "servers").asOpt[List[String]].getOrElse(List("localhost")).map(x => new ServerAddress(x))
    val connectTimeout = (mongoConfiguration \ "connectTimeout").asOpt[Int].getOrElse(1000)
    val socketTimeout = (mongoConfiguration \ "socketTimeout").asOpt[Int].getOrElse(10000)
    val databaseName = (mongoConfiguration \ "database").asOpt[String].getOrElse("locations")

    val options = MongoClientOptions(
      connectTimeout = connectTimeout,
      maxWaitTime = connectTimeout,
      heartbeatConnectTimeout = connectTimeout,
      socketTimeout = socketTimeout,
      readPreference = ReadPreference.SecondaryPreferred)

    val mongoClient = MongoClient(servers, options)
    val mongoService = new MongoServiceImpl(mongoClient(databaseName))

    new LocationService(mongoService)
  }

  def apply(mongoService: MongoService): LocationService = new LocationService(mongoService)

}

class LocationService(mongoService: MongoService) {

  private def saveUser(user: LocationUserModel): Future[Either[Exception, LocationUser]] = {

    mongoService.saveLocationUser(user) match {

      case Success(Some(locationUser)) => Future.successful(Right(
        LocationUser(locationUser.email,
          locationUser.deviceId,
          locationUser.lastLocation.coordinates(1),
          locationUser.lastLocation.coordinates(0))))
      case Success(None) => {
        Logger.error("Could not save the location user: " + user.email)
        Future.successful(Left(new Exception("Could not save the location user")))
      }
      case Failure(e) => {
        Logger.error("There was an error saving the location user", e)
        Future.successful(Left(new Exception("There was an error saving the location user")))
      }

    }
  }

  def registerUser(locationUser: LocationUser): Future[Either[Exception, LocationUser]] = {

    saveUser(locationUser.toLocationUserModel)

  }

  def updateUserLocation(email: String, latitude: Double, longitude: Double): Future[Either[Exception, LocationUser]] = {

    mongoService.findLocationUser(email) match {
      case Success(Some(user)) => {
        val toUpdateUser = user.copy(lastLocation = LocationPointModel(coordinates = List(longitude, latitude)))
        saveUser(toUpdateUser)
      }
      case Success(None) => {
        Logger.error("User not found: " + email)
        Future.successful(Left(new Exception("User not found")))
      }
      case Failure(e) => {
        Logger.error("There was an error saving the location user", e)
        Future.successful(Left(new Exception(e)))
      }
    }
  }

}