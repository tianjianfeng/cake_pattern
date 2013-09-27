package services

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.Play.current
import play.api.Logger
import play.api.libs.json._
import org.joda.time.DateTime
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.api.QueryOpts
import reactivemongo.core.commands.LastError
import reactivemongo.bson.BSONObjectID
import scala.util.Success
import scala.util.Failure
import scala.concurrent.{ future, promise }
import models.BaseModel
import scala.util.Try
import reactivemongo.bson.BSONDocument

trait DBServiceComponent[T <: BaseModel[T, U], U] {
    this: DBRepositoryComponent[T, U] =>
    val dbService: DBService

    class DBService {

        def findOneById(id: String)(implicit reader: Reads[T]): Future[Try[Option[T]]] = {
            dbRepository.findOneById(id)
        }

        def all(limit: Int, skip: Int)(implicit reader: Reads[T]): Future[Seq[T]] = {
            dbRepository.find(Json.obj(), limit, skip)
        }

        def insert(s: T)(implicit writer: Writes[T]): Future[Try[T]] = {
            dbRepository.insert(s)
        }

        def update(u: T)(implicit writer: Writes[T]): Future[Try[T]] = {
            dbRepository.update(u.withNewUpdatedDate(Some(DateTime.now)))
        }

        def remove(id: String): Future[Try[Boolean]] = {
            dbRepository.remove(id)
        }
    }
}

trait DBRepositoryComponent[T <: BaseModel[T, U], U] {

    def db = ReactiveMongoPlugin.db
    def coll: JSONCollection

    val dbRepository: DBRepository

    class DBRepository {
        implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

        def findOneById(id: String)(implicit reader: Reads[T]): Future[Try[Option[T]]] = {
            BSONObjectID.parse(id) match {
                case Success(bsonId) => {
                    val query = Json.obj("_id" -> bsonId)
                    val jsvalue = coll.find(query).one[JsValue]
                    for (js <- jsvalue) yield Success(js.get.asOpt[T])
                }
                case Failure(e) => Future(Failure(e))
            }
        }

        def find(sel: JsObject, limit: Int, skip: Int)(implicit reader: Reads[T]): Future[Seq[T]] = {
//                        val cursor = coll.find(sel).options(QueryOpts().skip(skip)).cursor[T]
            val cursor = coll.find(sel).options(QueryOpts().skip(skip).batchSize(limit)).cursor[JsValue].toList
            val filtered = cursor map { list =>
                for (jsValue <- list if (jsValue.asOpt[T] != None)) yield jsValue.as[T]
            }
            filtered
//                filter { jsValue =>
//                    val optT = jsValue.asOpt[T]
//                    optT != None
//                } 
//            }
//            cursor onSuccess
//            for (c <- cursor)
//            val filtered = cursor.filter { jsValue =>
//                    val optT = for (js <- jsValue) yield Success(js.asOpt[T])
//                    optT != None
//            } 
//            if (limit != 0) cursor.toList(limit) else cursor.toList
        }

        def insert(s: T)(implicit writer: Writes[T]): Future[Try[T]] = {
            recover(coll.insert(s)) {
                s
            }
        }

        def update(u: T)(implicit writer: Writes[T]): Future[Try[T]] = {
            val jsObject = Json.toJson(u).as[JsObject] - ("id")
            val updated = Json.obj("$set" -> jsObject)

            val selector = Json.obj("_id" -> BSONObjectID(u.id.get))
            recover(coll.update(selector, updated)) {
                u
            }
        }

        def remove(id: String): Future[Try[Boolean]] = {
            BSONObjectID.parse(id) match {
                case Success(bsonId) => {
                    val query = Json.obj("id" -> bsonId)
                    recover(coll.remove(query)) {
                        true
                    }
                }
                case Failure(e) => Future(Failure(e))
            }
        }

        def recover[S](operation: Future[LastError])(success: => S): Future[Try[S]] = {
            operation.map {
                lastError =>
                    lastError.inError match {
                        case true => {
                            Logger.error(s"DB operation did not perform successfully: [lastError=$lastError]")
                            Failure(DBServiceException(lastError))
                        }
                        case false => {
                            Success(success)
                        }
                    }
            }
        }
    }
}
