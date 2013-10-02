package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import models.User
import models.User._
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import play.modules.reactivemongo.json.BSONFormats.BSONObjectIDFormat
import reactivemongo.bson.BSONObjectID
import services.UserServiceComponent
import org.joda.time.DateTime
import scala.concurrent.Future
import models.UserStatus
import scala.util.Failure
import scala.util.Success
import services.BSONObjectIDException
import services.DBServiceException

trait UserCtrl extends Controller {
    this: UserServiceComponent =>

    def create = Action.async(parse.json) { implicit request =>
        val json = request.body
        val user = User(firstname = (json \ "firstname").as[String],
            lastname = (json \ "lastname").as[String])

        try {
            dbService.insert(user) map {
                case user: User => Created(Json.toJson(user))
                case _ => BadRequest
            }
        } catch {
            case e: DBServiceException => Future(InternalServerError)
        }

    }

    def findOneById(id: String) = Action.async {
        try {
            dbService.findOneById(id) map {
                case Some(user) => Ok(Json.toJson(user))
                case None => NotFound
            }
        } catch {
            case e: BSONObjectIDException => {
                Future(BadRequest)
            }
        }
    }

    def all(limit: Int, skip: Int) = Action.async { implicit request =>
        dbService.all(limit, skip) map { users =>
            Ok(Json.toJson(users))
        }
    }

    def update(id: String) = Action.async(parse.json) { implicit request =>

        dbService.findOneById(id) flatMap {
            case Some(user) => {
                val json = request.body
                val updated = (Json.toJson(user).as[JsObject] - ("id") ++ json.as[JsObject]).as[User]

                dbService.update(id, updated) map {
                    case user: User => Ok
                    case _ => InternalServerError
                } recover {
                    case e: BSONObjectIDException => BadRequest
                    case e: Exception => InternalServerError
                }
            }
            case None => Future(NotFound)
        } recover {
            case e: BSONObjectIDException => BadRequest
        }
    }
}

object UserController extends UserCtrl with UserServiceComponent {
    val dbService = new UserService
}