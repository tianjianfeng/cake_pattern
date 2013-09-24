package models

import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

case class User (
    firstname: String, 
    lastname: String,
    _id: Option[BSONObjectID] = None,
    createdDate: DateTime = DateTime.now,
    updatedDate: Option[DateTime] = None
    ) extends BaseModel[User] {

	def withNewCreatedDate(newCreatedDate: DateTime): User = this.copy(createdDate = newCreatedDate)
	def withNewUpdatedDate(newUpdatedDate: Option[DateTime]): User = this.copy(updatedDate = newUpdatedDate)
}

object User {
	implicit val jsonFormatter = Json.format[User]
}
