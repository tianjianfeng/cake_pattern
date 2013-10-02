package unit.controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.SECONDS

import org.joda.time.DateTime
import org.mockito.Mockito.when
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import akka.util.Timeout
import controllers.UserCtrl
import models.User
import models.UserStatus
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers.CREATED
import play.api.test.Helpers.INTERNAL_SERVER_ERROR
import play.api.test.Helpers.OK
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.contentType
import play.api.test.Helpers.status
import reactivemongo.core.commands.LastError
import services.BSONObjectIDException
import services.DBServiceException
import services.UserServiceComponent

class UserControllerUnitSpec extends Specification with Mockito {

    class TestController extends UserCtrl with UserServiceComponent {
        val dbService = mock[UserService] //(withSettings().defaultAnswer(RETURNS_DEEP_STUBS))
    }

    "User Controller" should {
        implicit val timeout = Timeout(FiniteDuration(5, SECONDS))

        "return CREATED when a user is created successfully" in {
            val controller = new TestController()
            val (firstname, lastname) = ("testfirstname", "testlastname")

            val user = User(firstname = firstname, lastname = lastname)

            when(controller.dbService.insert(user)).thenReturn(Future(user))

            val json = Json.obj(
                "firstname" -> firstname,
                "lastname" -> lastname)

            val req = FakeRequest().withBody(json)
            val result = controller.create(req)

            status(result) mustEqual CREATED

            contentType(result) must beSome("application/json")
            Json.parse(contentAsString(result)).as[User].firstname must equalTo(firstname)
            Json.parse(contentAsString(result)).as[User].lastname must equalTo(lastname)
        }

        "return Internal Server Error when a user is NOT created successfully" in {
            val controller = new TestController()
            val (firstname, lastname) = ("testfirstname", "testlastname")
            val json = Json.obj(
                "firstname" -> firstname,
                "lastname" -> lastname)

            val user = User(firstname = firstname, lastname = lastname)

            val lastError = LastError(ok = true, err = None, code = None, errMsg = Some("db exception"), originalDocument = None, updated = 1, updatedExisting = true)
            
            when(controller.dbService.insert(user)).thenThrow(DBServiceException(lastError))
            val req = FakeRequest().withBody(json)
            val result = controller.create(req)

            status(result) mustEqual INTERNAL_SERVER_ERROR
        }

        "return OK with user as json when a user is retrieved successfully by its id" in {
            val controller = new TestController()
            val id = "523adf223386b69b47c63431"

            val user = User(firstname = "abc", lastname = "edf", status = UserStatus.withName("Active"))

            when(controller.dbService.findOneById(id)).thenReturn(Future(Some(user)))

            val req = FakeRequest()
            val result = controller.findOneById(id)(req)

            status(result) mustEqual OK
            contentType(result) must beSome("application/json")
            contentAsString(result) must contain("firstname")
            contentAsString(result) must contain("lastname")
        }

        "return OK with a list of users as json when finding users with skip and limit" in {
            val controller = new TestController()

            val (limit, skip) = (0, 0)
            val users = List(User(firstname = "abc", lastname = "def"), User(firstname = "123", lastname = "456"))
            when(controller.dbService.findAll(limit, skip)).thenReturn(Future(users))

            val req = FakeRequest()
            val result = controller.all(limit, skip)(req)

            status(result) mustEqual OK
            contentType(result) must beSome("application/json")
            Json.parse(contentAsString(result)).as[Seq[User]].size must equalTo(2)
        }

        "return OK when a user is updated successfully by its id" in {
            val controller = new TestController()
            val id = "523adf223386b69b47c63431"

            val (updatedFirstname, updatedLastname) = ("testfirstname", "testlastname")
            val json = Json.obj(
                "firstname" -> updatedFirstname,
                "lastname" -> updatedLastname)

            val createdDate = DateTime.now
            val retrievedUser = User(id = Some(id), firstname = "firstname", lastname = "lastname", status = UserStatus.Active, createdDate = Some(createdDate))

            val updatedUser = User(firstname = updatedFirstname, lastname = updatedLastname, status = UserStatus.Active, createdDate = Some(createdDate))

            when(controller.dbService.findOneById(id)).thenReturn(Future(Some(retrievedUser)))
            when(controller.dbService.update(id, updatedUser)).thenReturn(Future(updatedUser))
            val req = FakeRequest().withBody(json)
            val result = controller.update(id)(req)

            status(result) mustEqual OK
        }
    }
}