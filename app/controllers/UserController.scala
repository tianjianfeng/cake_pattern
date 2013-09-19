package controllers

import play.api._
import play.api.mvc._
import services.UserRepositoryComponent
import services.UserServiceComponent
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.Action
import play.api.mvc.Controller
import models.User
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._
import play.api.libs.json.JsObject

trait UserCtrl extends Controller {
    this: UserServiceComponent with UserRepositoryComponent =>

    def create = Action(parse.json) { implicit request =>
        Async {
            dbService.insert((request.body).as[User]) map { either =>
                either match {
                    case Left(_) => InternalServerError
                    case Right(user) => Created
                }
            }
        }
    }

    def findOne (id: String) = Action {
        val query = Json.obj("_id" -> BSONObjectID(id))
        Async {
            dbService.findOne(query) map {
                case Some(user) => Ok(Json.toJson(user))
                case None => NotFound
            }
        }
    }
    def update(id: String) = Action(parse.json) { implicit request =>
        Async {
            val selector = Json.obj("_id" -> BSONObjectID(id))
            dbService.updatePartial(selector, request.body.asInstanceOf[JsObject]) map { either =>
                either match {
                    case Left(_) => InternalServerError
                    case Right(user) => Ok
                }
            }
        }
    }
    
    def specific (id: String) = Action {
        val query = Json.obj("_id" -> BSONObjectID(id))
        Async {
            dbService.specific(query) map {
                case Some(user) => Ok(Json.toJson(user))
                case None => NotFound
            }
        }
    }
}

object UserController extends UserCtrl with UserServiceComponent with UserRepositoryComponent {
    val dbService = new UserService
    val dbRepository = new UserRepository
}