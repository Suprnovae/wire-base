package controllers

import java.util.UUID
import models._
import org.apache.commons.lang3.StringUtils
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import scala.util._
import views._

object Withdrawals extends BaseController {
  val submitForm = Form(
    mapping(
      "secret"       -> nonEmptyText,
      "code"         -> nonEmptyText,
      "cashpoint_id" -> nonEmptyText
    )(WithdrawalForm.apply)(WithdrawalForm.unapply)
  )

  def add = SecureAction { implicit request =>
    submitForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      validForm => {
        val pending = Transaction.findByTokens(validForm.code, validForm.secret)
        val withdrawalAttempt: Try[Withdrawal] = {
          if(pending.isDefined) {
            Try(Withdrawal.create(
              Transaction.findById(pending.get.id).get,
              CashPoint.findById(UUID.fromString(validForm.cashpoint_id)).get
              ).get)
          } else {
            Try(throw new Exception("Transaction tokens not validated"))
          }
        }
        if(withdrawalAttempt.isSuccess) {
          Created
        } else {
          Conflict
        }
      }
    )
  }
}
