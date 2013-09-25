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

import models.BaseModel

trait DBServiceComponent[T <: BaseModel[T, U], U] {
	this: DBRepositoryComponent[T, U] =>
	val dbService: DBService

	class DBService {

		def findOneById(id: String)(implicit reader: Reads[T]): Future[Option[T]] = {
			dbRepository.findOneById(id)
		}

		def find(sel: JsObject, limit: Int, skip: Int)(implicit reader: Reads[T]): Future[Seq[T]] = {
			dbRepository.find(sel, limit, skip)
		}

		def insert(s: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
			dbRepository.insert(s.withNewCreatedDate(DateTime.now))
		}

		def update(id: String, u: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
			dbRepository.update(id, u.withNewUpdatedDate(Some(DateTime.now)))
		}

		def remove(id: String): Future[Either[ServiceException, Boolean]] = {
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

		def findOneById(id: String)(implicit reader: Reads[T]): Future[Option[T]] = {
			val query = Json.obj("_id" -> BSONObjectID(id))
			coll.find(query).one[T]
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

		def update(id: String, u: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
			val selector = Json.obj("_id" -> id)
			val updated = Json.obj("$set" -> u)
			recover(coll.update(selector, updated)) {
				u
			}
		}

		def remove(id: String): Future[Either[ServiceException, Boolean]] = {
			val query = Json.obj("_id" -> BSONObjectID(id))
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
