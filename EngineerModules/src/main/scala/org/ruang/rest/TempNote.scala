package org.ruang.rest

import com.twitter.finagle.http.path._
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.Future
import org.moon.http.RestApi

/**
 * 临时笔记
 *
 * @author RUANG
 */
class TempNote extends RestApi {
  override def apply(request: Request): Future[Response] = {
    val response = Response()

    Path(request.path) match {
      // 登录
      case Root / "put" =>
        if (request.method.equals(Method.Post)) {

        } else {
          // 请求错误
          response.setContentString(
            """
              |{
              |  "message": "need POST"
              |}
              |""".stripMargin)
          response.status(Status.MethodNotAllowed)
        }
      // 修改笔记
      case Root / "update" =>
        if (request.method.equals(Method.Put)) {

        } else {
          // 请求错误
          response.setContentString(
            """
              |{
              |  "message": "need PUT"
              |}
              |""".stripMargin)
          response.status(Status.MethodNotAllowed)
        }
      // 删除笔记
      case Root / "delete" =>
        if (request.method.equals(Method.Delete)) {

        } else {
          // 请求错误
          response.setContentString(
            """
              |{
              |  "message": "need DELETE"
              |}
              |""".stripMargin)
          response.status(Status.MethodNotAllowed)
        }
      // 获取笔记
      case Root / "get" =>
        if (request.method.equals(Method.Get)) {

        } else {
          // 请求错误
          response.setContentString(
            """
              |{
              |  "message": "need GET"
              |}
              |""".stripMargin)
          response.status(Status.MethodNotAllowed)
        }
      // 异常访问
      case _ =>
        response.setContentString(
          """
            |{
            |  "message": "not found"
            |}
            |""".stripMargin)
        response.status(Status.NotFound)
    }

    Future.value(response)
  }
}
