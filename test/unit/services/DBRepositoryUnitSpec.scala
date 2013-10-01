package unit.services

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import org.mockito.Mockito.when
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.withSettings
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.spy
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.functional.syntax.toInvariantFunctorOps
import play.api.libs.json.Reads.StringReads
import play.api.libs.json.Writes.StringWrites
import reactivemongo.core.commands.LastError
import services.DBRepositoryComponent
import play.api.libs.json.Json
import models.BaseModel
import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._
import play.api.libs.json.Reads
import helpers.EnumUtils
import play.api.libs.functional.syntax._
import models.TestModel
import models.TestStatus
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import reactivemongo.api.QueryOpts
import reactivemongo.core.commands.LastError
import services.DBServiceException
import reactivemongo.api.collections.default.BSONCollection

class DBRepositoryUnitSpec extends Specification with Mockito {

    class TestDBRepository extends DBRepositoryComponent{
        val dbRepository = new DBRepository
        def coll = mock[BSONCollection](withSettings().defaultAnswer(RETURNS_DEEP_STUBS))
    }

//    "Recover" should {
//
//        "return object if the operation is successful" in {
//            val operation = Future(LastError(ok = true, err = None, code = None, errMsg = None, originalDocument = None, updated = 1, updatedExisting = true))
//            val testDBRepo = new TestDBRepository()
//            val fakeObject = TestModel(title = "abc")
//
//            val result = testDBRepo.dbRepository.recover[TestModel](operation)(fakeObject)
//            val futureResult = result map {
//                case Success(result) => result
//                case Failure(_) =>
//            }
//
//            futureResult onComplete {
//                case Success(result) => result must equalTo(fakeObject)
//                case Failure(t) =>
//            }
//            1 must equalTo(1)
//        }
//
//        "return exception if the operation is failed" in {
//            val lastError = LastError(ok = false, err = Some("field"), code = Some(1), errMsg = Some("mymsg"), originalDocument = None, updated = 0, updatedExisting = false)
//            val operation = Future(lastError)
//            val testDBRepo = new TestDBRepository()
//            
//            val fakeObject = TestModel(title = "abc")
//
//            val result = testDBRepo.dbRepository.recover[TestModel](operation)(fakeObject)
//            val futureResult = result map {
//                case Success(t) =>
//                case Failure(e) => e.asInstanceOf[DBServiceException].message
//            }
//
//            futureResult onComplete {
//                case Failure(t) =>
//                case Success(msg) => {
//                    msg must equalTo("mymsg")
//                }
//            }
//            
//            1 must equalTo(1)
//        }
//    }
//
//    "findOneById" should {
//        "return an option object if it can be found by id" in {
//            val testDBRepo = new TestDBRepository()
//            val fakeObject = TestModel(title = "abc")
//
//            val id = "523adf223386b69b47c63431"
//            val query = Json.obj("_id" -> BSONObjectID("523adf223386b69b47c63431"))
//
//            when(testDBRepo.coll.find(query).one[TestModel]).thenReturn(Future(Some(fakeObject)))
//
//            val result = testDBRepo.dbRepository.findOneById(id)
//            val futureResult = for (r <- result) yield r.get.get
//
//            futureResult onComplete {
//                case Success(result) => result must equalTo(fakeObject)
//                case Failure(t) =>
//            }
//            1 must equalTo(1)
//        }
//    }
//    "find" should {
//        "return a list of object if retrieved successfully" in {
//            val testDBRepo = new TestDBRepository()
//            val fakeObjects = List(TestModel(title = "abc"), TestModel(title = "def"))
//
//            val query = Json.obj()
//
//            when(testDBRepo.coll.find(query).options(QueryOpts().skip(0).batchSize(0)).cursor[TestModel].toList).thenReturn(Future(fakeObjects))
//
//            val result = testDBRepo.dbRepository.find(query, 0, 0)
//
//            result onComplete {
//                case Success(myResult) => myResult must equalTo(fakeObjects)
//                case Failure(t) =>
//            }
//            1 must equalTo(1)
//        }
//    }
//
//    "insert" should {
//        "return the inserted object if the insertion is successful" in {
//            val testDBRepo = new TestDBRepository()
//            val fakeObject = TestModel(title = "abc").withNewCreatedDate(Some(DateTime.now))
//            val operation = Future(LastError(ok = true, err = None, code = None, errMsg = None, originalDocument = None, updated = 1, updatedExisting = true))
//
//            when(testDBRepo.coll.insert(fakeObject)).thenReturn(operation)
//
//            val result = testDBRepo.dbRepository.insert(fakeObject)
//            val futureResult = for (r <- result) yield r.get
//
//            futureResult onComplete {
//                case Success(myResult) => myResult must equalTo(fakeObject)
//                case Failure(t) =>
//            }
//            1 must equalTo(1)
//        }
//    }
//
//    "insert" should {
//        "return the updated object if the update is successful" in {
//            val testDBRepo = new TestDBRepository()
//            val id = "523adf223386b69b47c63431"
//            val fakeObject = TestModel(id=Some(id), title = "abc").withNewUpdatedDate(Some(DateTime.now))
//            val operation = Future(LastError(ok = true, err = None, code = None, errMsg = None, originalDocument = None, updated = 1, updatedExisting = true))
//
//            val selector = Json.obj("_id" -> BSONObjectID(fakeObject.id.get))
//            when(testDBRepo.coll.update(selector, fakeObject)).thenReturn(operation)
//
//            val result = testDBRepo.dbRepository.update(fakeObject)
//            val futureResult = for (r <- result) yield r.get
//
//            futureResult onComplete {
//                case Success(myResult) => myResult must equalTo(fakeObject)
//                case Failure(t) =>
//            }
//            1 must equalTo(1)
//        }
//    }

}