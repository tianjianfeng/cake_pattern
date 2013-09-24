package services

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.BaseModel
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.JsObject
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.QueryOpts
import reactivemongo.core.commands.LastError
import models.User
import org.joda.time.DateTime
import play.api.libs.json._

trait DBServiceComponent[T <: BaseModel] { this: DBRepositoryComponent[T] =>
    val dbService: DBService

    class DBService {

        def findOne(query: JsObject)(implicit reader: Reads[T]): Future[Option[T]] = {
            dbRepository.findOne(query)
        }

        def find(sel: JsObject, limit: Int, skip: Int)(implicit reader: Reads[T]): Future[Seq[T]] = {
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
        def find(sel: JsObject, limit: Int, skip: Int)(implicit reader: Reads[T]): Future[Seq[T]] = {
            val cursor = coll.find(sel).options(QueryOpts().skip(skip)).cursor[T]
            if (limit != 0) cursor.toList(limit) else cursor.toList
        }

        def insert(s: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
    		s.createdDate = Some(DateTime.now)
    		s.updatedDate = Some(DateTime.now)
    		println (s.createdDate)
            recover(coll.insert(s)) {
                s
            }
        }
        def updatePartial(s: JsObject, u: JsObject): Future[Either[ServiceException, JsObject]] = {
    		val updated = u ++ Json.obj("updatedDate" -> DateTime.now)
    		
            recover(coll.update(s, Json.obj("$set" -> updated))) {
                updated
            }
        }
        def update(s: JsObject, u: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
    		u.updatedDate = Some(DateTime.now)
    		val updated = Json.obj("$set" -> u)
            recover(coll.update(s, updated)) {
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
