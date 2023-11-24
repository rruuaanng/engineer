package org.ruang.router

import com.twitter.finagle.http.path._
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import org.moon.http.{ApiGateway, ForwardRequest}


/**
 * API网关
 */
class RequestRouter extends ApiGateway {
  override def apply(request: Request): Future[Response] = {
    val response = Response()

    Path(request.path) match {
      // 将请求转发到后端login RESTapi
      case Root / "login" =>
        // 转发请求并处理服务返回的请求
        new ForwardRequest()
          .send("localhost", "50010", request)
          .process({ x =>
            response.setContentString(x.getContentString())
          })
          .close

      // 转发请求到后端register RESTapi
      case Root / "register" =>
        new ForwardRequest()
          .send("localhost", "50010", request)
          .process({ x =>
            response.setContentString(x.getContentString())
          })
          .close
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
