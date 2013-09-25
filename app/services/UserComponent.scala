package services

import scala.concurrent.Future

import models.User
import models.UserStatus
import play.modules.reactivemongo.json.collection.JSONCollection

object UserComponentRegistry extends UserServiceComponent with UserRepositoryComponent {
    val dbRepository = new UserRepository
    val dbService = new UserService
}

trait UserServiceComponent extends DBServiceComponent[User, UserStatus.Value] { this: UserRepositoryComponent =>

    val dbService: UserService

    class UserService extends DBService
}

trait UserRepositoryComponent extends DBRepositoryComponent[User, UserStatus.Value]{

    val dbRepository: UserRepository
    
    def coll: JSONCollection = db[JSONCollection]("users")

    class UserRepository extends DBRepository
}