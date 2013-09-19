package services

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.joda.time.DateTime
import models.BaseModel
import play.api.Play.current
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json.__
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.LastError
import reactivemongo.api.QueryOpts
import reactivemongo.bson.BSONObjectID
import play.api.Logger
import reactivemongo.core.errors.DatabaseException
import models.User

trait DBServiceComponent[T <: BaseModel] { this: DBRepositoryComponent[T] =>
    val dbService: DBService

    class DBService {

        def findOne(query: JsObject)(implicit reader: Reads[T]): Future[Option[T]] = {
            dbRepository.findOne(query)
        }

        def find(sel: JsObject, limit: Int = 0, skip: Int = 0)(implicit reader: Reads[T]): Future[Seq[T]] = {
            dbRepository.find(sel, limit, skip)
        }

        def insert(s: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
            dbRepository.insert(s)
        }
        def updatePartial(s: JsObject, u: JsObject): Future[Either[ServiceException, JsObject]] = {
            dbRepository.updatePartial(s, u)
        }
        def update(s: JsObject, u: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
            dbRepository.update(s, u)
        }
        def remove(query: JsObject): Future[Either[ServiceException, Boolean]] = {
            dbRepository.remove(query)
        }
    }
}

trait DBRepositoryComponent[T <: BaseModel] {

    def db = ReactiveMongoPlugin.db
    def coll: JSONCollection

    val dbRepository: DBRepository

    class DBRepository {
        implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

        def findOne(query: JsObject)(implicit reader: Reads[T]): Future[Option[T]] = {
            coll.find(query).one[T]
        }
        def find(sel: JsObject, limit: Int = 0, skip: Int = 0)(implicit reader: Reads[T]): Future[Seq[T]] = {
            val cursor = coll.find(sel).options(QueryOpts().skip(skip)).cursor[T]
            if (limit != 0) cursor.toList(limit) else cursor.toList
        }

        def insert(s: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
            recover(coll.insert(s)) {
                s
            }
        }
        def updatePartial(s: JsObject, u: JsObject): Future[Either[ServiceException, JsObject]] = {
            recover(coll.update(s, u)) {
                u
            }
        }
        def update(s: JsObject, u: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
            recover(coll.update(s, u)) {
                u
            }
        }
        def remove(query: JsObject): Future[Either[ServiceException, Boolean]] = {
            recover(coll.remove(query)) {
                true
            }
        }

        def recover[S](operation: Future[LastError])(success: => S): Future[Either[ServiceException, S]] = {
            operation.map {
                lastError =>
                    lastError.inError match {
                        case true => {
                            Logger.error(s"DB operation did not perform successfully: [lastError=$lastError]")
                            Left(DBServiceException(lastError))
                        }
                        case false => {
                            Right(success)
                        }
                    }
            }
        }
    }
}
