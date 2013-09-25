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
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID
import scala.util.Success
import scala.util.Failure
import scala.concurrent.{ future, promise }

trait DBServiceComponent[T <: BaseModel[T]] { this: DBRepositoryComponent[T] =>
    val dbService: DBService

    class DBService {

        def findOneById(id: String)(implicit reader: Reads[T]): Future[Either[ServiceException, Option[T]]] = {
            dbRepository.findOneById(id)
        }

        def find(sel: JsObject, limit: Int, skip: Int)(implicit reader: Reads[T]): Future[Seq[T]] = {
            dbRepository.find(sel, limit, skip)
        }

        def insert(s: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
            dbRepository.insert(s)
        }

        def update(u: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
            dbRepository.update(u.withNewUpdatedDate(Some(DateTime.now)))
        }

        def remove(id: String): Future[Either[ServiceException, Boolean]] = {
            dbRepository.remove(id)
        }
    }
}

trait DBRepositoryComponent[T <: BaseModel[T]] {

    def db = ReactiveMongoPlugin.db
    def coll: JSONCollection

    val dbRepository: DBRepository

    class DBRepository {
        implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

        def findOneById(id: String)(implicit reader: Reads[T]): Future[Either[ServiceException, Option[T]]] = {
            constructBsonId(id) match {
                case Right(bsonObjectId) => {
                    val query = Json.obj("_id" -> bsonObjectId)

                    val jsvalue = coll.find(query).one[JsValue]
                    val optUser = for (js <- jsvalue) yield js.get.asOpt[T]

                    val p = promise[Either[ServiceException, Option[T]]]
                    optUser onComplete {
                        case Success(result) =>
                            p success Right(result)
                        case Failure(t) =>
                            p success Left(BSONObjectIDException(t.getMessage(), t))
                    }
                    p.future
                }
                case Left(e) => Future(Left(e))
            }
        }

        def find(sel: JsObject, limit: Int, skip: Int)(implicit reader: Reads[T]): Future[Seq[T]] = {
            val cursor = coll.find(sel).options(QueryOpts().skip(skip)).cursor[T]
            if (limit != 0) cursor.toList(limit) else cursor.toList
        }

        def insert(s: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
            recover(coll.insert(s)) {
                s
            }
        }

        def update(u: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
            val jsObject = Json.toJson(u).as[JsObject] - ("_id")
            val updated = Json.obj("$set" -> jsObject)

            val selector = Json.obj("_id" -> u._id.get)
            recover(coll.update(selector, updated)) {
                u
            }
        }

        def remove(id: String): Future[Either[ServiceException, Boolean]] = {
            constructBsonId(id) match {
                case Right(bsonObjectId) => {
                    val query = Json.obj("_id" -> bsonObjectId)
                    recover(coll.remove(query)) {
                        true
                    }
                }
                case Left(e) => Future(Left(e))
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
        def constructBsonId(id: String): Either[BSONObjectIDException, BSONObjectID] = {
            try {
                Right(BSONObjectID(id))
            } catch {
                case (e: NumberFormatException) => Left(BSONObjectIDException(e.getMessage(), e))
                case (e: IllegalArgumentException) => Left(BSONObjectIDException(e.getMessage(), e))
                case (e: Exception) => Left(BSONObjectIDException(e.getMessage(), e))
            }
        }
    }
}
