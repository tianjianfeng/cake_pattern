package models

import org.joda.time.DateTime
import helpers.EnumUtils
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json._
import play.api.libs.functional.syntax._
import helpers.EnumUtils.enumWrites

object TestStatus extends Enumeration {
    type TestStatus = Value
    val Active, Deleted = Value
}
case class TestModel(
    id: Option[String] = None,
    title: String,
    status: TestStatus.Value = TestStatus.Active,
    createdDate: DateTime = DateTime.now,
    updatedDate: Option[DateTime] = None) extends BaseModel[TestModel, TestStatus.Value] {

    def withNewCreatedDate(newCreatedDate: DateTime): TestModel = this.copy(createdDate = newCreatedDate)
    def withNewUpdatedDate(newUpdatedDate: Option[DateTime]): TestModel = this.copy(updatedDate = newUpdatedDate)

}
object TestModel {

    implicit val TestStatusReads: Reads[TestStatus.Value] = EnumUtils.enumReads(TestStatus)
    import EnumUtils.enumWrites

    implicit val TestReads: Reads[TestModel] = (
        (__ \ "id").readNullable[String] and
        (__ \ "title").read[String] and
        (__ \ "status").read[TestStatus.Value] and
        (__ \ "createdDate").read[DateTime] and
        (__ \ "updatedDate").readNullable[DateTime])(TestModel.apply _)

    implicit val TestWrites: Writes[TestModel] = (
        (__ \ "id").writeNullable[String] and
        (__ \ "title").write[String] and
        (__ \ "status").write[TestStatus.Value] and
        (__ \ "createdDate").write[DateTime] and
        (__ \ "updatedDate").writeNullable[DateTime])(unlift(TestModel.unapply))
}