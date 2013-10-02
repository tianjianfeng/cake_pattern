package services

import models.User
import models.UserStatus

import reactivemongo.api.collections.default.BSONCollection

object UserComponentRegistry extends UserServiceComponent { //with UserRepositoryComponent {
//    val dbRepository = new UserRepository
    val dbService = new UserService
}

trait UserServiceComponent extends DBServiceComponent[User, UserStatus.Value] { //this: UserRepositoryComponent =>

    override val dbService: UserService
    
    def coll: BSONCollection = db[BSONCollection]("users")

    class UserService extends DBService
}

//trait UserRepositoryComponent extends DBRepositoryComponent{
//
//    val dbRepository: UserRepository
//
//    def coll: BSONCollection = db[BSONCollection]("users")
//
//    class UserRepository extends DBRepository
//}