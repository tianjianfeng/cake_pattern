package services

import org.joda.time.DateTime
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.{ future, promise }
import play.api.Play.current
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.QueryOpts
import reactivemongo.core.commands.LastError
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._
import scala.util.{ Try, Success, Failure }
import models.BaseModel
import models.User
import models.UserStatus

trait DBServiceComponent[T <: BaseModel[T, U], U] {
    val dbService: DBService
    def db = ReactiveMongoPlugin.db
    def coll: BSONCollection

    class DBService {

        @throws (classOf[BSONObjectIDException])
        def findOneById(id: String)(implicit reader: BSONDocumentReader[T], ec: ExecutionContext): Future[Option[T]] = {

            BSONObjectID.parse(id) match {
                case Success(bsonId) =>
                    coll.find(BSONDocument("_id" -> bsonId)).one[BSONDocument] map {
                        case Some(bsonObj) => bsonObj.asOpt[T]
                        case None => None
                    }
                case Failure(e) => {
                    throw BSONObjectIDException(e.getMessage(), e) 
                }
            }
        }

        def all(limit: Int, skip: Int)(implicit reader: BSONDocumentReader[T], ec: ExecutionContext): Future[List[T]] = {
            val cursor = coll.find(BSONDocument()).options(QueryOpts().skip(skip)).cursor[BSONDocument]
            val cursorWithLimit = if (limit != 0) cursor.toList(limit) else cursor.toList

            val filtered = cursorWithLimit map { list =>
                for (bsonDoc <- list if (bsonDoc.asOpt[T] != None)) yield BSON.readDocument[T](bsonDoc)
            }
            filtered
        }

        @throws (classOf[DBServiceException])
        def insert(s: T)(implicit ec: ExecutionContext, writer: BSONDocumentWriter[T], reader: BSONDocumentReader[T]): Future[T] = {
            val t = s.withNewCreatedDate(Some(DateTime.now))
            recover(coll.insert(s.withNewCreatedDate(Some(DateTime.now)))) {
                s
            }
        }

        @throws (classOf[DBServiceException])
        def update(id: String, u: T)(implicit ec: ExecutionContext, writer: BSONDocumentWriter[T], reader: BSONDocumentReader[T]): Future[T] = {
            val selector = BSONDocument("_id" -> BSONObjectID(id))

            val updated = BSONDocument("$set" -> u.withNewUpdatedDate(Some(DateTime.now)))
            recover(coll.update(selector, updated)) {
                u
            }
        }
        @throws (classOf[DBServiceException])
        def recover[T](operation: Future[LastError])(success: => T)(implicit reader: BSONDocumentReader[T], ec: ExecutionContext): Future[T] = {
            operation.map {
                lastError =>
                    lastError.inError match {
                        case true => {
                            println ("error ==> ")
                            Logger.error(s"DB operation did not perform successfully: [lastError=$lastError]")
                            throw DBServiceException(lastError)
                        }
                        case false => {
                            success
                        }
                    }
            }
        }
    }
}