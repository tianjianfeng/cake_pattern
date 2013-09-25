package models

import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._
import play.api.data.Form._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._

case class User (
    _id: Option[BSONObjectID] = None,
    firstname: String, 
    lastname: String,
    createdDate: DateTime = DateTime.now,
    updatedDate: Option[DateTime] = None
    ) extends BaseModel[User] {
    
    
	def withNewCreatedDate(newCreatedDate: DateTime): User = this.copy(createdDate = newCreatedDate)
	def withNewUpdatedDate(newUpdatedDate: Option[DateTime]): User = this.copy(updatedDate = newUpdatedDate)
}
object User {
    implicit val fmt = Json.format[User]
}

