package models

import org.joda.time.DateTime
import play.api.libs.json.Json

case class User (firstname: String, lastname: String)

object User {
    implicit val fmt = Json.format[User]
}