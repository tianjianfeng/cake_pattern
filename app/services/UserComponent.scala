package services

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import models.User
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.MongoDriver
import play.api.mvc.Results.Status
import play.api.http.Status._
import play.api.libs.json.JsValue
import play.api.libs.ws.WS
import play.api.i18n.Lang
import reactivemongo.core.commands.GetLastError
import reactivemongo.bson.BSONDocument
import reactivemongo.api.QueryOpts
import play.api.libs.json.JsObject
import play.api.libs.json.Reads

object UserComponentRegistry extends UserServiceComponent with UserRepositoryComponent {
    val dbRepository = new UserRepository
    val dbService = new UserService
}

trait UserServiceComponent extends DBServiceComponent[User] { this: UserRepositoryComponent =>

    val dbService: UserService

    class UserService extends DBService {
        def specific(query: JsObject) = {
            dbRepository.specific(query)
        }
    }
}

trait UserRepositoryComponent extends DBRepositoryComponent[User]{

    val dbRepository: UserRepository
    
    def coll: JSONCollection = db[JSONCollection]("users")

    class UserRepository extends DBRepository {
        
        def specific(query: JsObject): Future[Option[User]] = { 
            coll.find(query).one[User]
        }
    }
}