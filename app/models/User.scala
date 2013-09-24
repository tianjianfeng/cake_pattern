package models

import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

case class User (
        _id: Option[BSONObjectID] = None,
        firstname: String, 
        lastname: String,
        override var createdDate: Option[DateTime] =None,
        override var updatedDate: Option[DateTime] = None
        ) extends BaseModel 

object User {
    implicit val fmt = Json.format[User]
}