package unit.services

import org.specs2.mutable.Specification
import services.DBRepositoryComponent
import org.specs2.mock.Mockito
import models.BaseModel
import scala.concurrent.Future
import reactivemongo.core.commands.LastError
import scala.concurrent.ExecutionContext.Implicits.global
import models.User
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.util.Success
import scala.util.Failure

class DBServiceUnitSpec extends Specification with Mockito {

    class TestDBRepository extends DBRepositoryComponent[BaseModel] {
        val dbRepository = new DBRepository
        def coll = mock[JSONCollection]
    }

    "DBService" should {
        "return object if the operation is successful" in {
            val operation = Future(LastError(ok = true, err = None, code = None, errMsg = None, originalDocument = None, updated = 1, updatedExisting = true))
            val testRepo = new TestDBRepository()
            val user = User(firstname = "abc", lastname = "def")

            val result = testRepo.dbRepository.recover[User](operation)(user)
            val futureUser = result map { either =>
                either match {
                    case Right(returnedUser) => returnedUser

                    case Left(_) =>
                }
            }

            futureUser onComplete {
                case Success(aUser) => aUser must equalTo(user)
            }
        }

        "return exception if the operation is failed" in {
            val operation = Future(LastError(ok = false, err = Some("field"), code = Some(1), errMsg = Some("mymsg"), originalDocument = None, updated = 0, updatedExisting = false))
            val testRepo = new TestDBRepository()
            val user = User(firstname = "abc", lastname = "def")

            val result = testRepo.dbRepository.recover[User](operation)(user)
            val futureResult = result map { either =>
                either match {
                    case Right(_) => 

                    case Left(e) => e.message
                }
            }

            futureResult onComplete {
                case Failure(t) => {

                }
                case Success(msg) => {
                    msg must equalTo ("mymsg")                    
                }
            }

        }

    }
}