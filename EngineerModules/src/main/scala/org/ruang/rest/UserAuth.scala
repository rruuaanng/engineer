package org.ruang.rest

import com.twitter.finagle.http.path._
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.Future
import org.moon.common.Json
import org.moon.http.RestApi
import org.ruang.controller.UserAuthControl

import java.util.Date

/**
 * 用户验证和注册
 *
 * @author RUANG
 */
class UserAuth extends RestApi {

  override def apply(request: Request): Future[Response] = {
    val response = Response()
    response.setContentTypeJson()

    Path(request.path) match {
      /**
       * 用户登录
       *
       * @note /login
       */
      case Root / "login" =>
        if (request.method.equals(Method.Post)) {
          // 当前请求时间
          val now = new Date()
          // 获取请求的账号密码
          val username = Option(request.getParam("username"))
          val passwd = Option(request.getParam("passwd"))

          // 检查参数是否为空
          if (UserAuthControl.loginParamIsNull(
            username, passwd, response))
            return Future.value(response)

          // 验证用户
          UserAuthControl.
            verifyAccount(username.get, passwd.get, now, response)
        } else {
          // 请求方法错误
          response.setContentString(Json.of(Map("message" -> "please use POST method")))
          response.status(Status.MethodNotAllowed)
        }

      /**
       * 用户注册
       *
       * @note /register
       */
      case Root / "register" =>
        if (request.method.equals(Method.Post)) {
          // 获取请求的账号密码
          val username = Option(request.getParam("username"))
          val passwd1 = Option(request.getParam("passwd1"))
          val passwd2 = Option(request.getParam("passwd2"))

          // 检查参数是否为空
          if (UserAuthControl.registerParamIsNull(
            username, passwd1, passwd2, response))
            return Future.value(response)

          // 检查用户是否已经注册
          UserAuthControl.checkAccountExists(
            username.get,
            passwd1.get,
            passwd2.get,
            response)
        } else {
          // 请求方法错误
          response.setContentString(Json.of(Map("message" -> "please use POST method")))
          response.status(Status.MethodNotAllowed)
        }

      // 异常响应
      case _ =>
        response.setContentString(Json.of(Map("message" -> "not found")))
        response.status(Status.NotFound)
    }

    // 返回响应
    Future.value(response)
  }
}