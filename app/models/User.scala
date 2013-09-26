package models

import org.joda.time.DateTime

import play.api.libs.json._
import play.api.libs.functional.syntax._

object UserStatus extends Enumeration {
	type UserStatus = Value
	val Active, Deleted = Value
}

object EnumUtils {
	def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
		def reads(json: JsValue): JsResult[E#Value] = json match {
			case JsString(s) => {
				try {
					JsSuccess(enum.withName(s))
				} catch {
					case _: NoSuchElementException => JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
				}
			}
			case _ => JsError("String value expected")
		}
	}

	implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
		def writes(v: E#Value): JsValue = JsString(v.toString)
	}
}

case class User (
	                firstname: String,
	                lastname: String,
	                id: Option[String] = None,
	                createdDate: DateTime = DateTime.now,
	                updatedDate: Option[DateTime] = None,
	                status: UserStatus.Value = UserStatus.Active
	                ) extends BaseModel[User, UserStatus.Value] {

	def withNewCreatedDate (newCreatedDate: DateTime): User = this.copy (createdDate = newCreatedDate)
	def withNewUpdatedDate (newUpdatedDate: Option[DateTime]): User = this.copy (updatedDate = newUpdatedDate)

	//implicit val writer = User.UserBSONWriter
	//implicit val reader = User.UserBSONReader
}

object User {

	implicit val UserStatusReads: Reads[UserStatus.Value] = EnumUtils.enumReads(UserStatus)
	import EnumUtils.enumWrites

	implicit val UserReads: Reads[User] = (
			(__ \ "firstname").read[String] and
			(__ \ "lastname").read[String] and
			(__ \ "id").readNullable[String] and
			(__ \ "createdDate").read[DateTime] and
			(__ \ "updatedDate").readNullable[DateTime] and
			(__ \ "status").read[UserStatus.Value]
		)(User.apply _)

	implicit val UserWrites: Writes[User] = (
			(__ \ "firstname").write[String] and
			(__ \ "lastname").write[String] and
			(__ \ "id").writeNullable[String] and
			(__ \ "createdDate").write[DateTime] and
			(__ \ "updatedDate").writeNullable[DateTime] and
			(__ \ "status").write[UserStatus.Value]
		)(unlift(User.unapply))

//	implicit object UserBSONReader extends BSONReader[BSONDocument, User] {
//		def read (document: BSONDocument): User = {
//			Logger.debug("H1")
//			User (
//				document.getAs[BSONString]("firstname").map (_.value).getOrElse ("<UNDEFINED>"),
//				document.getAs[BSONString]("lastname").map (_.value).getOrElse ("<UNDEFINED>"),
//				Option (document.getAs[BSONObjectID]("_id").toString),
//				document.getAs[BSONDateTime]("createdDate").map (bdt => new DateTime (bdt.value)).getOrElse (new DateTime),
//				document.getAs[BSONDateTime]("updatedDate").map (bdt => new DateTime (bdt.value)),
//				document.getAs[BSONString]("status").map (status => UserStatus.withName (status.value)).getOrElse (UserStatus.Deleted)
//			)
//		}
//	}
//
//	implicit object UserBSONWriter extends BSONWriter[User, BSONDocument] {
//		def write (user: User): BSONDocument = {
//			Logger.debug("H2")
//			val bson = BSONDocument (
//				"firstname" -> BSONString (user.firstname),
//				"lastname" -> BSONString (user.lastname),
//				"_id" -> user.id.map (id => BSONObjectID (id)).getOrElse (BSONObjectID.generate),
//				"createdDate" -> BSONDateTime(user.createdDate.getMillis),
//				"updatedDate" -> user.updatedDate.map(_.getMillis),
//				"status" -> BSONString (user.status.toString)
//			)
//
//			bson
//		}
//	}
}
