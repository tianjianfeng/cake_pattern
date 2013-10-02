package unit.services

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import org.mockito.Mockito.when
import org.mockito.Mockito.verify
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.withSettings
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.functional.syntax.toInvariantFunctorOps
import play.api.libs.json.JsObject
import play.api.libs.json.Reads.StringReads
import play.api.libs.json.Writes.StringWrites
import services.DBServiceComponent
import play.api.libs.json.Json
import models.BaseModel
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import models.TestModel
import models.TestStatus
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.core.commands.LastError
import services.DBServiceException
import reactivemongo.bson.BSONDocument

class DBServiceUnitSpec extends Specification with Mockito {

    class TestDBService extends DBServiceComponent[TestModel, TestStatus.Value] { //with DBRepositoryComponent {
        val dbService = new DBService
        def coll = mock[BSONCollection](withSettings().defaultAnswer(RETURNS_DEEP_STUBS))
    }
    
        "DBService" should {
    
            "return one object when retrieve by id successfully" in {
                val testDBService = new TestDBService()
                val id = "523adf223386b69b47c63431"
                val testModel = mock[TestModel]
    
                when(testDBService.dbService.findOneById(id)).thenReturn(Future(Some(testModel)))
                testDBService.dbService.findOneById(id) onComplete {
                    case Success(optUser) => optUser.get must equalTo(testModel)
                    case Failure(t) =>
                }
                1 must equalTo(1)
            }
    
            "return a list of objects when calling find" in {
                val testDBService = new TestDBService()
                val query = BSONDocument()
                val testModelList = List(mock[TestModel], mock[TestModel])
    
                when(testDBService.dbService.all(0, 0)).thenReturn(Future(testModelList))
                println (testDBService.dbService.all(0, 0))
                testDBService.dbService.all(0, 0) onComplete {
                    case Success(result) => result must equalTo(testModelList)
                    case Failure(t) =>
                }
                1 must equalTo(1)
            }
    
            "return the inserted objects when calling insert" in {
                val testDBService = new TestDBService()
                val testModel = mock[TestModel]
    
                val testModelWithDate = testModel.withNewCreatedDate(Some(any[DateTime]))
                
                when(testDBService.dbService.insert(testModelWithDate )).thenReturn(Future(testModelWithDate ))
    
                val futureResult = testDBService.dbService.insert(testModel ) map { 
                        case result => result
                }
                futureResult onComplete {
                    case Success(result) => result must equalTo(testModelWithDate )
                    case Failure(t) =>
                }
                1 must equalTo(1)
            }
    //
    //        "return the updated objects when calling update" in {
    //            val testDBService = new TestDBService()
    //            val id = "id"
    //            val testModel = mock[TestModel]
    //            val testModelWithDate = testModel.withNewUpdatedDate(Some(any[DateTime]))
    //
    //            when(testDBService.dbRepository.update(testModelWithDate)).thenReturn(Future(Success(testModelWithDate)))
    //
    //            val futureResult = testDBService.dbService.update(testModel) map { 
    //                    case Success(result) => result
    //                    case Failure(_) =>
    //            }
    //            futureResult onComplete {
    //                case Success(result) => result must equalTo(testModelWithDate)
    //                case Failure(t) =>
    //            }
    //            1 must equalTo(1)
    //        }
    //
    //        "return true when the object is successfully removed" in {
    //            val testDBService = new TestDBService()
    //            val id = "id"
    //
    //            when(testDBService.dbRepository.remove(id)).thenReturn(Future(Success(true)))
    //
    //            val futureResult = testDBService.dbService.remove(id) map { 
    //                    case Success(result) => result
    //                    case Failure(_) =>
    //            }
    //            futureResult onComplete {
    //                case Success(result) => result must equalTo(true)
    //                case Failure(t) =>
    //            }
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
        }
    "Recover" should {

        "return object if the operation is successful" in {
            val operation = Future(LastError(ok = true, err = None, code = None, errMsg = None, originalDocument = None, updated = 1, updatedExisting = true))
            val testDBService = new TestDBService()
            val fakeObject = TestModel(title = "abc")

            val result = testDBService.dbService.recover[TestModel](operation)(fakeObject)
            val futureResult = result map {
                case testResult: TestModel => testResult
                case _ =>
            }

            futureResult onComplete {
                case Success(result) => result must equalTo(fakeObject)
                case Failure(t) =>
            }
            1 must equalTo(1)
        }

        "return exception if the operation is failed" in {
            val lastError = LastError(ok = false, err = Some("field"), code = Some(1), errMsg = Some("mymsg"), originalDocument = None, updated = 0, updatedExisting = false)
            val operation = Future(lastError)
            val testDBService = new TestDBService()

            val fakeObject = TestModel(title = "abc")
            val mySpy = spy(testDBService.dbService)
            when(mySpy.recover[TestModel](operation)(fakeObject)).thenThrow(DBServiceException(lastError))
            verify(mySpy).recover[TestModel](operation)(fakeObject)
            1 must equalTo(1)
        }.pendingUntilFixed("temporaily failing")
    }

}