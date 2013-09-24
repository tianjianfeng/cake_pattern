package models

import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._

abstract class BaseModel[T] {
	val _id: Option[BSONObjectID]
	val createdDate: DateTime
	val updatedDate: Option[DateTime]
	
	def withNewCreatedDate(createdDate: DateTime): T
	def withNewUpdatedDate(updatedDate: Option[DateTime]): T
}