package models

import reactivemongo.bson.{ BSONDocument, BSONObjectID }
import org.joda.time.DateTime
import play.api.libs.json.Reads
import helpers.EnumUtils
import reactivemongo.bson.BSONHandler
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDateTime
import play.api.Logger
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONDocumentWriter

abstract class BaseModel[T, U] {
    val id: Option[String]
    val createdDate: Option[DateTime]
    val updatedDate: Option[DateTime]
    val status: U

    def withNewCreatedDate(createdDate: Option[DateTime]): T
    def withNewUpdatedDate(updatedDate: Option[DateTime]): T
}

//object BaseModel {
//
//    implicit val UserStatusReads: Reads[UserStatus.Value] = EnumUtils.enumReads(UserStatus)
//    import EnumUtils.enumWrites
//
//    implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
//        def read(time: BSONDateTime) = new DateTime(time.value)
//        def write(jdtime: DateTime) = BSONDateTime(jdtime.getMillis)
//    }
//    implicit object BaseModelBSONReader extends BSONDocumentReader[BaseModel[T, U]] {
//        def read(document: BSONDocument): BaseModel = {
//            Logger.debug("H1")
//            User(
//                Option(document.getAs[BSONObjectID]("_id").toString),
//                document.getAs[BSONDateTime]("createdDate").map(bdt => new DateTime(bdt.value)),
//                document.getAs[BSONDateTime]("updatedDate").map(bdt => new DateTime(bdt.value)))
//        }
//    }
//
//    implicit object BaseModelBSONWriter extends BSONDocumentWriter[BaseModel] {
//        def write(basemodel: BaseModel): BSONDocument = {
//            Logger.debug("H2")
//            val bson = BSONDocument(
//                "_id" -> basemodel.id.map(id => BSONObjectID(id)).getOrElse(BSONObjectID.generate),
//                "status" -> BSONString(basemodel.status.toString))
//            if (basemodel.createdDate.isDefined)
//                bson.add(BSONDocument("createdDate" -> BSONDateTime(basemodel.createdDate.get.getMillis)))
//            if (basemodel.updatedDate.isDefined)
//                bson.add(BSONDocument("updatedDate" -> BSONDateTime(basemodel.updatedDate.get.getMillis)))
//
//            bson
//        }
//    }
//}