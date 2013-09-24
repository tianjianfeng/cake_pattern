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
            
            println ("user: " + user)
            
        dbService.insert(user) map { either =>
            either match {
                case Left(_) => {
                    InternalServerError
                }
                case Right(user) => {
                    Created(Json.toJson(user))
                }
            }
        }
    }

    def findOneById(id: String) = Action.async {
        dbService.findOneById(id) map {
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
    
    def update(id: String) = Action.async(parse.json) { implicit request =>
        val selector = Json.obj("_id" -> BSONObjectID(id))
        
        dbService.findOneById(id) flatMap {
            case Some(user) => {
                val updated = request.body.as[User].copy(createdDate = user.createdDate)
                dbService.update(id, updated) map { either =>
                    either match {
                        case Left(_) => InternalServerError
                        case Right(user) => Ok
                    }
                } 
            }
            case None => Future(NotFound)
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