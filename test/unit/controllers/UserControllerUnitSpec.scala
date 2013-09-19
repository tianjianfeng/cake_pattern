package unit.controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.apache.commons.codec.digest.DigestUtils
import org.joda.time.DateTime
import org.joda.time.Period
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.when
import org.mockito.Mockito.withSettings
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import controllers.UserCtrl
import services.DBServiceComponent
import models.User
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers.BAD_REQUEST
import play.api.test.Helpers.CREATED
import play.api.test.Helpers.INTERNAL_SERVER_ERROR
import play.api.test.Helpers.NOT_FOUND
import play.api.test.Helpers.OK
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.session
import play.api.test.Helpers.contentType
import play.api.test.Helpers.status
import play.api.test.WithApplication
import play.modules.reactivemongo.json.BSONFormats.BSONDocumentFormat
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONInteger
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.BSONString
import reactivemongo.bson.Producer.nameValue2Producer
import services.UserServiceComponent
import reactivemongo.core.commands.LastError
import reactivemongo.bson.BSONInteger
import services.UserRepositoryComponent
import services.ServiceException
import play.modules.reactivemongo.json.BSONFormats._

class UserControllerUnitSpec extends Specification with Mockito {

    class TestController extends UserCtrl with UserServiceComponent with UserRepositoryComponent {
        val dbService = mock[UserService] //(withSettings().defaultAnswer(RETURNS_DEEP_STUBS))
        val dbRepository = mock[UserRepository] //(withSettings().defaultAnswer(RETURNS_DEEP_STUBS))
    }

    "User Controller" should {

        "return CREATED when a user is created successfully" in {
            val controller = new TestController()
            val json = Json.obj(
                "firstname" -> "testfirstname",
                "lastname" -> "testsurname")

            val user = json.as[User]

            when(controller.dbService.insert(json.as[User])).thenReturn(Future(Right(user)))
            val req = FakeRequest().withBody(json)
            val result = controller.create(req)

            status(result) mustEqual CREATED
        }

        "return Internal Server Error when a user is NOT created successfully" in {
            val controller = new TestController()
            val json = Json.obj(
                "firstname" -> "testfirstname",
                "lastname" -> "testsurname")

            val user = json.as[User]

            when(controller.dbService.insert(json.as[User])).thenReturn(Future(Left(any[ServiceException])))
            val req = FakeRequest().withBody(json)
            val result = controller.create(req)

            status(result) mustEqual INTERNAL_SERVER_ERROR
        }

        "return OK with user as json when a user is retrieved successfully by its id" in {
            val controller = new TestController()
            val id = "523adf223386b69b47c63431"

            val user = User(firstname = "abc", lastname = "edf")

            val selector = Json.obj("_id" -> BSONObjectID(id))
            when(controller.dbService.findOne(selector)).thenReturn(Future(Some(user)))
            val req = FakeRequest()
            val result = controller.findOne(id)(req)

            status(result) mustEqual OK
            contentType(result) must beSome("application/json")
            contentAsString(result) must contain("firstname")
            contentAsString(result) must contain("lastname")
        }

        "return OK when a user is updated successfully by its id" in {
            val controller = new TestController()
            val id = "523adf223386b69b47c63431"

            val json = Json.obj(
                "firstname" -> "update",
                "lastname" -> "update")

            val selector = Json.obj("_id" -> BSONObjectID(id))
            when(controller.dbService.updatePartial(selector, json)).thenReturn(Future(Right(json)))
            val req = FakeRequest().withBody(json)
            val result = controller.update(id)(req)

            status(result) mustEqual OK
        }

        "return OK with one user if the specificAction is successful" in {
            val controller = new TestController()
            val id = "523adf223386b69b47c63431"

            val user = User(firstname = "abc", lastname = "edf")

            val selector = Json.obj("_id" -> BSONObjectID(id))
            when(controller.dbService.specific(selector)).thenReturn(Future(Some(user)))
            val req = FakeRequest()
            val result = controller.specific(id)(req)

            status(result) mustEqual OK
            contentType(result) must beSome("application/json")
            contentAsString(result) must contain("firstname")
            contentAsString(result) must contain("lastname")
        }
    }
}