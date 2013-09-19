package models

import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.Json

abstract class BaseModel (
        _id: Option[BSONObjectID] = None, 
        createdDate: Option[DateTime] = None, 
        updatedDate: Option[DateTime] = None)
