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
import scala.concurrent.Future

trait UserCtrl extends Controller {
    this: UserServiceComponent with UserRepositoryComponent =>

    def create = Action.async(parse.json) { implicit request =>
        val json = request.body
        val user = User(firstname = (json \ "firstname").as[String],
            lastname = (json \ "lastname").as[String])

        dbService.insert(user) map {
            case Left(_) => {
                InternalServerError
            }
            case Right(user) => {
                Created(Json.toJson(user))
            }
        }
    }

    def findOneById(id: String) = Action.async {
        dbService.findOneById(id) map {
            case Left(_) => BadRequest
            case Right(optionUser) => {
                optionUser match {
                    case Some(user) => Ok(Json.toJson(user))
                    case None => NotFound
                }
            }
        }
    }

    def all(limit: Int, skip: Int) = Action.async { implicit request =>
        dbService.all (limit, skip) map { users =>
            Ok(Json.toJson(users))
        }
    }

    def update(id: String) = Action.async(parse.json) { implicit request =>

        dbService.findOneById(id) flatMap {
            case Left(e) => Future(BadRequest)
            case Right(optionUser) => {
                optionUser match {
                    case None => Future(NotFound)
                    case Some(user) => {
                        val json = request.body
                        val updated = user.copy(id=Some(id), firstname= (json \ "firstname").as[String], lastname=(json \ "lastname").as[String])
                        dbService.update(updated) map { either =>
                            either match {
                                case Left(_) => InternalServerError
                                case Right(user) => Ok
                            }
                        }
                    }
                }
            }
        }
    }

    //    def specific(id: String) = Action.async {
    //        val query = Json.obj("_id" -> BSONObjectID(id))
    //        dbService.specific(query) map {
    //            case Some(user) => Ok(Json.toJson(user))
    //            case None => NotFound
    //        }
    //    }
}

object UserController extends UserCtrl with UserServiceComponent with UserRepositoryComponent {
    val dbService = new UserService
    val dbRepository = new UserRepository
}