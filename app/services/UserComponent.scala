package services

import models.User
import models.UserStatus
import reactivemongo.api.collections.default.BSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import reactivemongo.api.FailoverStrategy

object UserComponentRegistry extends UserServiceComponent { 
    val dbService = new UserService
}

trait UserServiceComponent extends DBServiceComponent[User, UserStatus.Value] { 

    override val dbService: UserService
    
    def coll: BSONCollection = db[BSONCollection]("users")
        
    class UserService extends DBService
}
