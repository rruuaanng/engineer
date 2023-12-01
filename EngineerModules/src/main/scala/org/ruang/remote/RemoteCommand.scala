package org.ruang.remote

import com.twitter.finagle.http.path._
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.Future
import io.circe.parser.parse
import org.moon.common.Json
import org.moon.http.RestApi

/**
 * 接收远程发送的命令
 */
class RemoteCommand extends RestApi {
  /*
  shell将接收到的命令封装为JSON，通过POST请求发送到modules（本文件）
  然后对POST包中的命令进行解析并调用对应接口，将结果已字符串的形式返回到shell
   */
  override def apply(request: Request): Future[Response] = {
    val response = Response()
    response.setContentTypeJson()

    Path(request.path) match {
      /**
       * 接收命令
       *
       * @note /command
       */
      case Root / "command" =>
        if (request.method.equals(Method.Post)) {
          val body = request.getContentString()
          val json = parse(body)

          json match {
            case Right(value) =>
              val path = value.hcursor.get[String]("op").getOrElse("")
              path match {
                case "send" =>
                  println("send")
                case "note" =>
                  println("note")
              }
          }
          response.setContentString(Json.of(Map("message" -> "execute success")))
          response.status(Status.Ok)
        } else {
          // 请求方法错误
          response.setContentString(Json.of(Map("message" -> "need POST")))
          response.status(Status.MethodNotAllowed)
        }
      // 异常请求
      case _ =>
        response.setContentString(Json.of(Map("message" -> "not found")))
        response.status(Status.NotFound)
    }

    Future.value(response)
  }
}
