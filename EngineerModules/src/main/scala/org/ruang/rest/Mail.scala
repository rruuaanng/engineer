package org.ruang.rest

import com.twitter.finagle.http.path._
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import io.circe.parser.parse
import org.moon.common.Json
import org.moon.http.RestApi

class Mail extends RestApi {
  override def apply(request: Request): Future[Response] = {
    val response = Response()
    response.setContentTypeJson()

    Path(request.path) match {
      /**
       * 发送/接收邮件
       *
       * @note /mail/receive
       * @note /mail/send
       */
      case Root / "mail" / command =>
        val body = request.getContentString()
        val json = parse(body)

        // 匹配命令
        command match {
          // 若为send则调用发送邮件
          case "send" => println("send")
          // 若为receive则调用接收邮件
          case "receive" => println("receive")
          // 未知命令则抛出
          case _ =>
            response.setContentString(Json.of(Map("message" -> "unknown command")))
            response.status(Status.NotFound)
        }
      // 异常请求
      case _ =>
        response.setContentString(Json.of(Map("message" -> "not found")))
        response.status(Status.NotFound)
    }

    Future.value(response)
  }
}
