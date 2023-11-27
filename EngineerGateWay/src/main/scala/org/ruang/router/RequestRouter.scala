package org.ruang.router

import com.twitter.finagle.http.path._
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import org.moon.common.Json
import org.moon.http.{ApiGateway, ForwardRequest}


/**
 * API网关
 */
class RequestRouter extends ApiGateway {
  override def apply(request: Request): Future[Response] = {
    val response = Response()

    // 路由选择可以优化为路径参数

    Path(request.path) match {
      // 将请求转发到后端login RESTapi
      case Root / "login" => forwardRequest("localhost", "50010", request, response)
      // 转发请求到后端register RESTapi
      case Root / "register" => forwardRequest("localhost", "50010", request, response)
      // 转发请求到后端tempNote RESTapi
      case Root / "note" / _ => forwardRequest("localhost", "50020", request, response)
      // 异常访问
      case _ =>
        response.setContentString(Json.of(Map("message" -> "not found")))
        response.status(Status.NotFound)
    }
    Future.value(response)
  }

  private def forwardRequest(host: String, port: String,
                             request: Request, response: Response): Unit = {
    new ForwardRequest()
      .send(host, port, request)
      .process({ x =>
        response.setContentString(x.getContentString())
      })
      .close
  }

}
