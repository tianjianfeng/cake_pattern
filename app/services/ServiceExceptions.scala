package services

import reactivemongo.core.commands.LastError

trait ServiceException extends Exception {
    val message: String
    val nestedException: Throwable
}

case class DBServiceException(
    message: String,
    lastError: Option[LastError] = None,
    nestedException: Throwable = null) extends ServiceException

object DBServiceException {
    def apply(lastError: LastError): ServiceException = {
        DBServiceException(lastError.errMsg.getOrElse(lastError.message), Some(lastError))
    }
}