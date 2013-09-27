package helpers

import play.api.libs.json.Reads
import play.api.libs.json.JsResult
import play.api.libs.json.JsValue
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import play.api.libs.json.Writes

object EnumUtils {
	def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
		def reads(json: JsValue): JsResult[E#Value] = json match {
			case JsString(s) => {
				try {
					JsSuccess(enum.withName(s))
				} catch {
					case _: NoSuchElementException => JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
				}
			}
			case _ => JsError("String value expected")
		}
	}

	implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
		def writes(v: E#Value): JsValue = JsString(v.toString)
	}
}