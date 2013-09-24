package unit.services

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.functional.syntax.toInvariantFunctorOps
import play.api.libs.json.Reads.StringReads
import play.api.libs.json.Writes.StringWrites
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.LastError
import services.DBRepositoryComponent
import play.api.libs.json.Json
import models.BaseModel
import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

class DBRepositoryUnitSpec extends Specification with Mockito {

    case class TestModel(
        title: String,
        _id: Option[BSONObjectID] = None,
        createdDate: DateTime = DateTime.now,
        updatedDate: Option[DateTime] = None) extends BaseModel[TestModel] {

        def withNewCreatedDate(newCreatedDate: DateTime): TestModel = this.copy(createdDate = newCreatedDate)
        def withNewUpdatedDate(newUpdatedDate: Option[DateTime]): TestModel = this.copy(updatedDate = newUpdatedDate)

    }
        
    object TestModel {
        implicit val fmt = Json.format[TestModel]
    }

    class TestDBRepository extends DBRepositoryComponent[TestModel] {
        val dbRepository = new DBRepository
        def coll = mock[JSONCollection]
    }

    "DBRepository" should {

        "return object if the operation is successful" in {
            val operation = Future(LastError(ok = true, err = None, code = None, errMsg = None, originalDocument = None, updated = 1, updatedExisting = true))
            val testDBRepository = new TestDBRepository()
            val fakeObject = TestModel(title = "abc")

            val result = testDBRepository.dbRepository.recover[TestModel](operation)(fakeObject)
            val futureResult = result map { either =>
                either match {
                    case Right(result) => result
                    case Left(_) =>
                }
            }

            futureResult onComplete {
                case Success(result) => result must equalTo(fakeObject)
                case Failure(t) =>
            }
            1 must equalTo(1)
        }

        "return exception if the operation is failed" in {
            val operation = Future(LastError(ok = false, err = Some("field"), code = Some(1), errMsg = Some("mymsg"), originalDocument = None, updated = 0, updatedExisting = false))
            val testDBRepository = new TestDBRepository()
            val fakeObject = TestModel(title = "abc")

            val result = testDBRepository.dbRepository.recover[TestModel](operation)(fakeObject)
            val futureResult = result map { either =>
                either match {
                    case Right(_) =>
                    case Left(e) => e.message
                }
            }

            futureResult onComplete {
                case Failure(t) =>
                case Success(msg) => {
                    msg must equalTo("mymsg")
                }
            }
            1 must equalTo(1)
        }

    }
}