package models

import reactivemongo.bson.{BSONDocument, BSONObjectID}

import org.joda.time.DateTime

abstract class BaseModel[T, U] {
	val id: Option[String]
	val createdDate: Option[DateTime]
	val updatedDate: Option[DateTime]
	val status: U
	
	def withNewCreatedDate(createdDate: Option[DateTime]): T
	def withNewUpdatedDate(updatedDate: Option[DateTime]): T
}