package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import models.User
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import play.modules.reactivemongo.json.BSONFormats.BSONObjectIDFormat
import reactivemongo.bson.BSONObjectID
import services.UserRepositoryComponent
import services.UserServiceComponent
import org.joda.time.DateTime

trait UserCtrl extends Controller {
    this: UserServiceComponent with UserRepositoryComponent =>

    def create = Action.async(parse.json) { implicit request =>
        val json = request.body
        val user = User(firstname = (json \ "firstname").as[String],
            lastname = (json \ "lastname").as[String])

        dbService.insert(user) map { either =>
            either match {
                case Left(_) => {
                    InternalServerError
                }
                case Right(user) => {
                    Created
                }
            }
        }
    }

    def findOne(id: String) = Action.async {
        val query = Json.obj("_id" -> BSONObjectID(id))
        dbService.findOne(query) map {
            case Some(user) => Ok(Json.toJson(user))
            case None => NotFound
        }
    }

    def find(limit: Int, skip: Int) = Action.async(parse.json) { implicit request =>
        val query = request.body.asInstanceOf[JsObject]
        dbService.find(query, limit, skip) map { users =>
            Ok(Json.toJson(users))
        }
    }
    def updatePartial(id: String) = Action.async(parse.json) { implicit request =>
        val selector = Json.obj("_id" -> BSONObjectID(id))
        dbService.updatePartial(selector, request.body.asInstanceOf[JsObject]) map { either =>
            either match {
                case Left(_) => InternalServerError
                case Right(user) => Ok
            }
        }
    }

    def specific(id: String) = Action.async {
        val query = Json.obj("_id" -> BSONObjectID(id))
        dbService.specific(query) map {
            case Some(user) => Ok(Json.toJson(user))
            case None => NotFound
        }
    }
}

object UserController extends UserCtrl with UserServiceComponent with UserRepositoryComponent {
    val dbService = new UserService
    val dbRepository = new UserRepository
}