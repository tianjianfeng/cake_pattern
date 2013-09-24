package services

import scala.concurrent.Future

import models.User
import play.api.libs.json.JsObject
import play.modules.reactivemongo.json.collection.JSONCollection

object UserComponentRegistry extends UserServiceComponent with UserRepositoryComponent {
    val dbRepository = new UserRepository
    val dbService = new UserService
}

trait UserServiceComponent extends DBServiceComponent[User] { this: UserRepositoryComponent =>

    val dbService: UserService

    class UserService extends DBService {
        def specific(query: JsObject) = {
//            dbRepository.specific(query)
        }
    }
}

trait UserRepositoryComponent extends DBRepositoryComponent[User]{

    val dbRepository: UserRepository
    
    def coll: JSONCollection = db[JSONCollection]("users")

    class UserRepository extends DBRepository
}