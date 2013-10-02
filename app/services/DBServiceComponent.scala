package services

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import org.joda.time.DateTime

import models.BaseModel
import play.api.Logger
import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.QueryOpts
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSON
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentIdentity
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.BSONObjectIDIdentity
import reactivemongo.bson.BSONValue.ExtendedBSONValue
import reactivemongo.bson.Producer.nameValue2Producer
import reactivemongo.core.commands.LastError

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
                        case Some(bsonObj) => {
                            bsonObj.asOpt[T]
                        }
                        case None => {
                            None
                        }
                    }
                case Failure(e) => {
                    throw BSONObjectIDException(e.getMessage(), e) 
                }
            }
        }

        def findAll(limit: Int, skip: Int)(implicit reader: BSONDocumentReader[T], ec: ExecutionContext): Future[List[T]] = {
            val cursor = coll.find(BSONDocument()).options(QueryOpts().skip(skip)).cursor[BSONDocument]
            val cursorWithLimit = if (limit != 0) cursor.toList(limit) else cursor.toList

            val filtered = cursorWithLimit map { list =>
                for (bsonDoc <- list if (bsonDoc.asOpt[T] != None)) yield BSON.readDocument[T](bsonDoc)
            }
            filtered
        }

        @throws (classOf[DBServiceException])
        def insert(s: T)(implicit ec: ExecutionContext, writer: BSONDocumentWriter[T], reader: BSONDocumentReader[T]): Future[T] = {
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