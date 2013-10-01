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

import scala.util.{Try,Success,Failure}

import models.BaseModel

trait DBServiceComponent[T <: BaseModel[T, U], U] {
    this: DBRepositoryComponent =>
    val dbService: DBService

    class DBService {

        def findOneById[T](id: String)(implicit reader: BSONReader[BSONDocument, T], ec: ExecutionContext): Future[Option[T]] = {
            val p = promise[Option[T]]

	        BSONObjectID.parse(id)  match {
		        case Success(bsonId) =>
			        dbRepository.findOneById(bsonId) onComplete {
				        case Success(optBson) => {
					        optBson match {
						        case Some(bsonObj) => p success Some(BSON.readDocument[T](bsonObj))
						        case None => p success None
					        }
				        }
				        case Failure(e) => p failure e
			        }
		        case Failure(e) => p failure BSONObjectIDException(e.getMessage(), e)
	        }

            p.future
        }

        def all(limit: Int, skip: Int)(implicit reader: BSONReader[BSONDocument, T], ec: ExecutionContext): Future[List[T]] = {
            dbRepository.find(BSONDocument(), limit, skip) map { listBson =>
                for (obj <- listBson) yield BSON.readDocument[T](obj)
            }
        }

        def recover[T](operation: Future[LastError])(success: => T)(implicit reader: BSONReader[BSONDocument, T], ec: ExecutionContext): Future[Try[T]] = {
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

        def insert(s: T)(implicit ec: ExecutionContext, writer: BSONDocumentWriter[T], reader: BSONReader[BSONDocument, T]): Future[Try[T]] = {
            //            val bsonDoc = writer.writeTry(s)
            val t = s.withNewCreatedDate(Some(DateTime.now))
            println (t)
            recover(coll.insert(s.withNewCreatedDate(Some(DateTime.now)))) {
                s
            }
        }

        def update(id: String, u: T)(implicit ec: ExecutionContext, writer: BSONDocumentWriter[T], reader: BSONReader[BSONDocument, T]): Future[Try[T]] = {
            val selector = BSONDocument("_id" -> BSONObjectID(id))
            
            val updated = BSONDocument("$set" -> u.withNewUpdatedDate(Some(DateTime.now)))
            println ("user ==> " + BSONDocument.pretty(updated))
            recover(coll.update(selector, updated)) {
                u
            }
        }
    }
}

trait DBRepositoryComponent {

    def db = ReactiveMongoPlugin.db
    def coll: BSONCollection

    val dbRepository: DBRepository

    class DBRepository {
        implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

        def findOneById(id: BSONObjectID): Future[Option[BSONDocument]] = {
            coll.find(BSONDocument("_id" -> id)).one[BSONDocument]
        }

        def find(sel: BSONDocument, limit: Int, skip: Int): Future[List[BSONDocument]] = {
            val cursor = coll.find(sel).options(QueryOpts().skip(skip).batchSize(limit)).cursor[BSONDocument].toList
            val filtered = cursor map { list =>
                for (bsonValue <- list if (bsonValue.asOpt[BSONDocument] != None)) yield bsonValue.as[BSONDocument]
            }
            filtered
        }

    }
}
