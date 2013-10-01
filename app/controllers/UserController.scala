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
import services.UserRepositoryComponent
import services.UserServiceComponent
import org.joda.time.DateTime
import scala.concurrent.Future
import models.UserStatus
import scala.util.Failure
import scala.util.Success
import services.BSONObjectIDException

trait UserCtrl extends Controller {
    this: UserServiceComponent with UserRepositoryComponent =>

    def create = Action.async(parse.json) { implicit request =>
        val json = request.body
        val user = User(firstname = (json \ "firstname").as[String],
            lastname = (json \ "lastname").as[String])

        dbService.insert(user) map {
            case Failure(_) => InternalServerError
            case Success(user) => Created(Json.toJson(user))
        }
    }

    def findOneById(id: String) = Action.async {
        println ("dbservice: ==> " + dbService)
        println ("id: " + id)
        println ("another ==> " + dbService.findOneById(id) )
        dbService.findOneById(id) map {
            case Some(user) => {
                println ("user: " + user)
                Ok(Json.toJson(user))
            }
            case None => {
                println ("not found")
                NotFound
            }
        } recover {
            case e: BSONObjectIDException => {
                println ("bson exception")
                BadRequest
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
//                val updated = user.copy(firstname = (json \ "firstname").as[String], lastname = (json \ "lastname").as[String])
                val updated = User(firstname = (json \ "firstname").as[String], 
                        lastname = (json \ "lastname").as[String], 
                        status = UserStatus.withName((json \ "status").as[String]),
                        createdDate = user.createdDate)
                dbService.update(id, updated) map {
                    case Failure(_) => InternalServerError
                    case Success(user) => Ok
                }
            }
            case None => Future(NotFound)
        } recover {
            case e: BSONObjectIDException => BadRequest
        }
    }
}

object UserController extends UserCtrl with UserServiceComponent with UserRepositoryComponent {
    val dbService = new UserService
    val dbRepository = new UserRepository
}