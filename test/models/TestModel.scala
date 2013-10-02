package models

import org.joda.time.DateTime
import helpers.EnumUtils
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json._
import play.api.libs.functional.syntax._
import helpers.EnumUtils.enumWrites
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDocumentWriter

object TestStatus extends Enumeration {
    type TestStatus = Value
    val Active, Deleted = Value
}
case class TestModel(
    id: Option[String] = None,
    title: String,
    status: TestStatus.Value = TestStatus.Active,
    createdDate: Option[DateTime] = None,
    updatedDate: Option[DateTime] = None) extends BaseModel[TestModel, TestStatus.Value] {

    def withNewCreatedDate(newCreatedDate: Option[DateTime]): TestModel = this.copy(createdDate = newCreatedDate)
    def withNewUpdatedDate(newUpdatedDate: Option[DateTime]): TestModel = this.copy(updatedDate = newUpdatedDate)

    implicit val writer = TestModel.TestModelBSONWriter
    implicit val reader = TestModel.TestModelBSONReader
}
object TestModel {

    implicit val TestStatusReads: Reads[TestStatus.Value] = EnumUtils.enumReads(TestStatus)
    import EnumUtils.enumWrites

    implicit object TestModelBSONReader extends BSONDocumentReader[TestModel] {
        def read(document: BSONDocument): TestModel = {
            TestModel(
                Option(document.getAs[BSONObjectID]("_id").get.stringify),
                document.getAs[BSONString]("title").map(_.value).getOrElse("<UNDEFINED>"),
                document.getAs[BSONString]("status").map(status => TestStatus.withName(status.value)).getOrElse(TestStatus.Deleted),
                document.getAs[BSONDateTime]("createdDate").map(bdt => new DateTime(bdt.value)),
                document.getAs[BSONDateTime]("updatedDate").map(bdt => new DateTime(bdt.value)))
        }
    }

    implicit object TestModelBSONWriter extends BSONDocumentWriter[TestModel] {
        def write(testModel: TestModel): BSONDocument = {
            var bson = BSONDocument(
                //                    "_id" -> user.id.map(id => BSONObjectID(id)),
                "title" -> BSONString(testModel.title),
                "status" -> BSONString(testModel.status.toString))

            if (testModel.createdDate.isDefined)
                bson = bson.add(BSONDocument("createdDate" -> BSONDateTime(testModel.createdDate.get.getMillis)))
            if (testModel.updatedDate.isDefined)
                bson = bson.add(BSONDocument("updatedDate" -> BSONDateTime(testModel.updatedDate.get.getMillis)))
            bson
        }
    }
}