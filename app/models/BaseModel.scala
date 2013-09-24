package models

import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._

trait BaseModel {
    def _id: Option[BSONObjectID]
    var createdDate: Option[DateTime]
    var updatedDate: Option[DateTime]
}

