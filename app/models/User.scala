package models

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._
import helpers.EnumUtils
import helpers.MyDate
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONString
import play.api.Logger
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.BSONDateTime
import play.api.data.format.Formats._
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.Macros
import reactivemongo.bson.BSONHandler
import models.UserStatus
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._

object UserStatus extends Enumeration {
    type UserStatus = Value
    val Active, Deleted = Value
}

case class User(
    id: Option[String] = None,
    firstname: String,
    lastname: String,
    createdDate: Option[DateTime] = None,
    updatedDate: Option[DateTime] = None,
    status: UserStatus.Value = UserStatus.Active
    ) extends BaseModel[User, UserStatus.Value] {

        def withNewCreatedDate(newCreatedDate: Option[DateTime]): User = this.copy(createdDate = newCreatedDate)
        def withNewUpdatedDate(newUpdatedDate: Option[DateTime]): User = this.copy(updatedDate = newUpdatedDate)

    implicit val writer = User.UserBSONWriter
    implicit val reader = User.UserBSONReader
}

object User {

    implicit val UserStatusReads: Reads[UserStatus.Value] = EnumUtils.enumReads(UserStatus)
    import EnumUtils.enumWrites

    implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
        def read(time: BSONDateTime) = new DateTime(time.value)
        def write(jdtime: DateTime) = BSONDateTime(jdtime.getMillis)
    }


//    implicit val userFormat = Macros.handler[User]

        implicit val fmt = Json.format[User]

    //    implicit val UserReads: Reads[User] = (
    //        (__ \ "id").readNullable[String] and
    //        (__ \ "firstname").read[String] and
    //        (__ \ "lastname").read[String] and
    //        (__ \ "createdDate").readNullable[DateTime] and
    //        (__ \ "updatedDate").readNullable[DateTime] and
    //        (__ \ "status").read[UserStatus.Value])(User.apply _)
    //
    //    implicit val UserWrites: Writes[User] = (
    //        (__ \ "id").writeNullable[String] and
    //        (__ \ "firstname").write[String] and
    //        (__ \ "lastname").write[String] and
    //        (__ \ "createdDate").writeNullable[DateTime] and
    //        (__ \ "updatedDate").writeNullable[DateTime] and
    //        (__ \ "status").write[UserStatus.Value])(unlift(User.unapply))

        implicit object UserBSONReader extends BSONDocumentReader[User] {
            def read(document: BSONDocument): User = {
                Logger.debug("H1")
                User(
                    Option(document.getAs[BSONObjectID]("_id").get.stringify),
                    document.getAs[BSONString]("firstname").map(_.value).getOrElse("<UNDEFINED>"),
                    document.getAs[BSONString]("lastname").map(_.value).getOrElse("<UNDEFINED>"),
                    document.getAs[BSONDateTime]("createdDate").map(bdt => new DateTime(bdt.value)),
                    document.getAs[BSONDateTime]("updatedDate").map(bdt => new DateTime(bdt.value)),
                    document.getAs[BSONString]("status").map(status => UserStatus.withName(status.value)).getOrElse(UserStatus.Deleted))
            }
        }
    
        implicit object UserBSONWriter extends BSONDocumentWriter[User] {
            def write(user: User): BSONDocument = {
                Logger.debug("H2")
                var bson = BSONDocument(
//                    "_id" -> user.id.map(id => BSONObjectID(id)),
                    "firstname" -> BSONString(user.firstname),
                    "lastname" -> BSONString(user.lastname),
                    "status" -> BSONString(user.status.toString))
                    
                    println ("create date: ==> " + user.createdDate.isDefined)
                if (user.createdDate.isDefined)
                    bson = bson.add(BSONDocument("createdDate" -> BSONDateTime(user.createdDate.get.getMillis)))
                if (user.updatedDate.isDefined)
                    bson = bson.add(BSONDocument("updatedDate" -> BSONDateTime(user.updatedDate.get.getMillis)))
    println ("bson ==> " + BSONDocument.pretty(bson))
                bson
            }
        }
}


