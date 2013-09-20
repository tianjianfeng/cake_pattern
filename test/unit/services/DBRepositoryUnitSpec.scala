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

class DBRepositoryUnitSpec extends Specification with Mockito {

    case class FakeModel(title: String)
    object FakeModel {
        implicit val fmt = Json.format[FakeModel]
    }

    class TestDBRepository extends DBRepositoryComponent[FakeModel] {
        val dbRepository = new DBRepository
        def coll = mock[JSONCollection]
    }

    "DBRepository" should {

        "return object if the operation is successful" in {
            val operation = Future(LastError(ok = true, err = None, code = None, errMsg = None, originalDocument = None, updated = 1, updatedExisting = true))
            val testDBRepository = new TestDBRepository()
            val fakeObject = FakeModel(title = "abc")

            val result = testDBRepository.dbRepository.recover[FakeModel](operation)(fakeObject)
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
        }

        "return exception if the operation is failed" in {
            val operation = Future(LastError(ok = false, err = Some("field"), code = Some(1), errMsg = Some("mymsg"), originalDocument = None, updated = 0, updatedExisting = false))
            val testDBRepository = new TestDBRepository()
            val fakeObject = FakeModel(title = "abc")

            val result = testDBRepository.dbRepository.recover[FakeModel](operation)(fakeObject)
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

        }

    }
}