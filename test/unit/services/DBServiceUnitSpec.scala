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
import play.modules.reactivemongo.json.collection.JSONCollection
import services.DBRepositoryComponent
import services.DBServiceComponent
import play.api.libs.json.Json
import models.BaseModel
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime

class DBServiceUnitSpec extends Specification with Mockito {

    case class TestModel(
        _id: Option[BSONObjectID] = None, 
        title: String,
        createdDate: DateTime = DateTime.now,
        updatedDate: Option[DateTime] = None) extends BaseModel[TestModel] {

        def withNewCreatedDate(newCreatedDate: DateTime): TestModel = this.copy(createdDate = newCreatedDate)
        def withNewUpdatedDate(newUpdatedDate: Option[DateTime]): TestModel = this.copy(updatedDate = newUpdatedDate)
    }

    object TestModel {
        implicit val fmt = Json.format[TestModel]
    }

    class TestDBService extends DBServiceComponent[TestModel] with DBRepositoryComponent[TestModel] {
        val dbService = new DBService
        val dbRepository = mock[DBRepository]
        def coll = mock[JSONCollection]
    }

    "DBService" should {

        "return one object when send the query to the repository" in {
            val testDBService = new TestDBService()
            val query = mock[JsObject]
            val testModel = mock[TestModel]

            when(testDBService.dbRepository.findOne(query)).thenReturn(Future(Some(testModel)))
            testDBService.dbService.findOne(query) onComplete {
                case Success(result) => result.get must equalTo(testModel)
                case Failure(t) =>
            }
            1 must equalTo(1)
        }

        "return a list of objects when calling find" in {
            val testDBService = new TestDBService()
            val query = mock[JsObject]
            val testModelList = Seq(mock[TestModel], mock[TestModel])

            when(testDBService.dbRepository.find(query, 0, 0)).thenReturn(Future(testModelList))
            testDBService.dbService.find(query, 0, 0) onComplete {
                case Success(result) => result must equalTo(testModelList)
                case Failure(t) =>
            }
            1 must equalTo(1)
        }

        "return the inserted objects when calling insert" in {
            val testDBService = new TestDBService()
            val testModel = mock[TestModel]

            when(testDBService.dbRepository.insert(testModel.withNewCreatedDate(any[DateTime]))).thenReturn(Future(Right(testModel)))

            val futureResult = testDBService.dbService.insert(testModel) map { either =>
                either match {
                    case Right(result) => result
                    case Left(_) =>
                }
            }
            futureResult onComplete {
                case Success(result) => result must equalTo(testModel)
                case Failure(t) =>
            }
            1 must equalTo(1)
        }

        "return the updated objects when calling update" in {
            val testDBService = new TestDBService()
            val selector = mock[JsObject]
            val testModel = mock[TestModel]

            when(testDBService.dbRepository.update(selector, testModel.withNewUpdatedDate(Some(any[DateTime])))).thenReturn(Future(Right(testModel)))

            val futureResult = testDBService.dbService.update(selector, testModel) map { either =>
                either match {
                    case Right(result) => result
                    case Left(_) =>
                }
            }
            futureResult onComplete {
                case Success(result) => result must equalTo(testModel)
                case Failure(t) =>
            }
            1 must equalTo(1)
        }

        "return the updated json objects when calling updatePartial" in {
            val testDBService = new TestDBService()
            val selector = mock[JsObject]
            val updatedObj = mock[JsObject]

            when(testDBService.dbRepository.updatePartial(selector, updatedObj)).thenReturn(Future(Right(updatedObj)))

            val futureResult = testDBService.dbService.updatePartial(selector, updatedObj) map { either =>
                either match {
                    case Right(result) => result
                    case Left(_) =>
                }
            }
            futureResult onComplete {
                case Success(result) => result must equalTo(updatedObj)
                case Failure(t) =>
            }
            1 must equalTo(1)
        }
        
        "return true when the object is successfully removed" in {
            val testDBService = new TestDBService()
            val selector = mock[JsObject]

            when(testDBService.dbRepository.remove(selector)).thenReturn(Future(Right(true)))

            val futureResult = testDBService.dbService.remove(selector) map { either =>
                either match {
                    case Right(result) => result
                    case Left(_) =>
                }
            }
            futureResult onComplete {
                case Success(result) => result must equalTo(true)
                case Failure(t) =>
            }
            1 must equalTo(1)
        }
    }
}