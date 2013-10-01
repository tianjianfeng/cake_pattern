package unit.services

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import org.mockito.Mockito.when
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.functional.syntax.toInvariantFunctorOps
import play.api.libs.json.JsObject
import play.api.libs.json.Reads.StringReads
import play.api.libs.json.Writes.StringWrites
import services.DBRepositoryComponent
import services.DBServiceComponent
import play.api.libs.json.Json
import models.BaseModel
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import models.TestModel
import models.TestStatus
import reactivemongo.api.collections.default.BSONCollection

class DBServiceUnitSpec extends Specification with Mockito {

    class TestDBService extends DBServiceComponent[TestModel, TestStatus.Value] with DBRepositoryComponent {
        val dbService = new DBService
        val dbRepository = mock[DBRepository]
        def coll = mock[BSONCollection]
    }
//
//    "DBService" should {
//
//        "return one object when retrieve by id successfully" in {
//            val testDBService = new TestDBService()
//            val id = "id"
//            val testModel = mock[TestModel]
//
//            when(testDBService.dbRepository.findOneById(id)).thenReturn(Future(Some(testModel)))
//            testDBService.dbService.findOneById(id) onComplete {
//                case Success(optUser) => optUser.get.get must equalTo(testModel)
//                case Failure(t) =>
//            }
//            1 must equalTo(1)
//        }
//
//        "return a list of objects when calling find" in {
//            val testDBService = new TestDBService()
//            val query = Json.obj()
//            val testModelList = List(mock[TestModel], mock[TestModel])
//
//            when(testDBService.dbRepository.find(query, 0, 0)).thenReturn(Future(testModelList))
//            println (testDBService.dbService.all(0, 0))
//            testDBService.dbService.all(0, 0) onComplete {
//                case Success(result) => result must equalTo(testModelList)
//                case Failure(t) =>
//            }
//            1 must equalTo(1)
//        }
//
//        "return the inserted objects when calling insert" in {
//            val testDBService = new TestDBService()
//            val testModel = mock[TestModel]
//
//            val testModelWithDate = testModel.withNewCreatedDate(Some(any[DateTime]))
//            
//            when(testDBService.dbRepository.insert(testModelWithDate )).thenReturn(Future(Success(testModelWithDate )))
//
//            val futureResult = testDBService.dbService.insert(testModel ) map { 
//                    case Success(result) => result
//                    case Failure(_) =>
//            }
//            futureResult onComplete {
//                case Success(result) => result must equalTo(testModelWithDate )
//                case Failure(t) =>
//            }
//            1 must equalTo(1)
//        }
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
}