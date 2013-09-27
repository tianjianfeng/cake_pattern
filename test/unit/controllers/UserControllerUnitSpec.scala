package unit.controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.mockito.Mockito.when
import org.mockito.Mockito.doReturn
import org.mockito.Matchers._
import org.specs2.mock.Mockito
import org.mockito.Matchers.argThat
import org.specs2.mutable.Specification
import controllers.UserCtrl
import models.User
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers.CREATED
import play.api.test.Helpers.INTERNAL_SERVER_ERROR
import play.api.test.Helpers.OK
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.contentType
import play.api.test.Helpers.status
import play.modules.reactivemongo.json.BSONFormats.BSONObjectIDFormat
import reactivemongo.bson.BSONObjectID
import services.ServiceException
import services.UserRepositoryComponent
import services.UserServiceComponent
import play.api.test.Helpers.defaultAwaitTimeout
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import helpers.UserMatcher
import org.hamcrest.Matcher
import scala.util.Success
import scala.util.Failure
import org.powermock.api.mockito.PowerMockito
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.core.classloader.annotations.PrepareForTest
import org.junit.runner.RunWith
import helpers.MyDate

class UserControllerUnitSpec extends Specification with Mockito {

    class TestController extends UserCtrl with UserServiceComponent with UserRepositoryComponent {
        val dbService = mock[UserService]
        val dbRepository = mock[UserRepository]
    }

    "User Controller" should {

        "return CREATED when a user is created successfully" in {
            val controller = new TestController()
            val (firstname, lastname) = ("testfirstname", "testlastname")

            val user = User(firstname = firstname, lastname = lastname)

            when(controller.dbService.insert(user)).thenReturn(Future(Success(user)))

            val json = Json.obj(
                "firstname" -> firstname,
                "lastname" -> lastname)

            val req = FakeRequest().withBody(json)
            val result = controller.create(req)
            status(result) mustEqual CREATED
            contentType(result) must beSome("application/json")
            contentAsString(result) must contain("firstname")
            contentAsString(result) must contain("lastname")
        }

        "return Internal Server Error when a user is NOT created successfully" in {
            val controller = new TestController()
            val (firstname, lastname) = ("testfirstname", "testlastname")
            val json = Json.obj(
                "firstname" -> firstname,
                "lastname" -> lastname)

            val user = User(firstname = firstname, lastname = lastname)

            when(controller.dbService.insert(user)).thenReturn(Future(Failure(any[ServiceException])))
            val req = FakeRequest().withBody(json)
            val result = controller.create(req)

            status(result) mustEqual INTERNAL_SERVER_ERROR
        }

        "return OK with user as json when a user is retrieved successfully by its id" in {
            val controller = new TestController()
            val id = "523adf223386b69b47c63431"

            val user = User(firstname = "abc", lastname = "edf")

            when(controller.dbService.findOneById(id)).thenReturn(Future(Success(Some(user))))
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
            when(controller.dbService.all(limit, skip)).thenReturn(Future(users))

            val req = FakeRequest()
            val result = controller.all(limit, skip)(req)

            status(result) mustEqual OK
            contentType(result) must beSome("application/json")
            Json.parse(contentAsString(result)).as[Seq[User]].size must equalTo(2)
        }
        "return OK when a user is updated successfully by its id" in {
            val controller = new TestController()
            val id = "523adf223386b69b47c63431"

            val (firstname, lastname) = ("testfirstname", "testlastname")
            val json = Json.obj(
                "firstname" -> firstname,
                "lastname" -> lastname)

            val user = User(id = Some(id), firstname = firstname, lastname = lastname)

            when(controller.dbService.findOneById(id)).thenReturn(Future(Success(Some(user))))
            when(controller.dbService.update(user)).thenReturn(Future(Success(user)))
            val req = FakeRequest().withBody(json)
            val result = controller.update(id)(req)

            status(result) mustEqual OK
        }
    }
}