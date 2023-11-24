package org.ruang.rest

import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.{JWSAlgorithm, JWSHeader, JWSObject, Payload}
import com.nimbusds.jwt.JWTClaimsSet
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.Future
import org.moon.config.{Configure, JdbcConfig}
import org.moon.http.RestApi
import org.moon.store.ConnectorFactory

import java.util.Date

/**
 * 用户验证和注册
 *
 * @author RUANG
 */
class UserAuth extends RestApi {

  // JWT头部
  // Base64解码
  private val jwtHeader = new JWSHeader.Builder(JWSAlgorithm.HS256).build()

  // JWT签名密钥
  private val key = new MACSigner(Configure().getKey("jwtKey"))

  // JWT主体
  // Base64解码
  private val jwtBody = new JWTClaimsSet.Builder()

  // JDBC设置
  private val dbConfig = JdbcConfig()
    .withHost("localhost")
    .withPort("3306")
    .withUser("root")
    .withPasswd("123456")
    .withDbName("engineer")
    .withDbType("mysql")
    .withDriver("com.mysql.cj.jdbc.Driver")
    .build()

  // 数据库连接客户端
  private val client = ConnectorFactory.createJdbc("user_auth", dbConfig)

  override def apply(request: Request): Future[Response] = {
    val response = Response()

    Path(request.path) match {
      // 登录
      case Root / "login" =>
        if (request.method.equals(Method.Post)) {
          // 当前请求时间
          val now = new Date()
          // 获取请求的账号密码
          val username = Option(request.getParam("username"))
          val passwd = Option(request.getParam("passwd"))

          // 账号和密码是否为空
          (username, passwd) match {
            case (Some(x1), Some(x2)) =>
              if (x1.isEmpty || x2.isEmpty) {
                response.setContentString(
                  """
                    |{
                    |  "message": "username and passwd isn't null",
                    |  "status: "0"
                    |}
                    |""".stripMargin)
                response.status(Status.Unauthorized)
                // 直接返回响应
                return Future.value(response)
              }
          }

          val user = client.get("username", username.get)
          if (user.next()) {
            // 验证账号和密码
            if (user.getString("username").equals(username.get)
              && user.getString("passwd").equals(passwd.get)) {
              // 登录成功
              // JWT主体
              val body = jwtBody
                .subject(username.get)
                .issueTime(now)
                .expirationTime(new Date(now.getTime + 3600000)) // 一小时后过期
                .build()
              val jwt = new JWSObject(jwtHeader, new Payload(body.toJSONObject))

              // 签名并返回JWT令牌
              jwt.sign(key)
              response.setContentString(
                s"""
                   |{
                   |  "message": "login success",
                   |  "status": "1",
                   |  "token": "${jwt.serialize()}"
                   |}
                   |""".stripMargin)
              response.status(Status.Ok)
            } else {
              // 密码错误
              response.setContentString(
                """
                  |{
                  |  "message": "passwd error"
                  |}
                  |""".stripMargin)
              response.status(Status.Unauthorized)
            }
          } else {
            // 用户不存在
            response.setContentString(
              """
                |{
                |  "message": "user don't exists"
                |}
                |""".stripMargin)
            response.status(Status.Unauthorized)
          }

        } else {
          // 请求方法错误
          response.setContentString(
            """
              |{
              |  "message": "please use POST method"
              |}
              |""".stripMargin)
          response.status(Status.MethodNotAllowed)
        }
      // 注册
      case Root / "register" =>
        if (request.method.equals(Method.Post)) {
          // 获取请求的账号密码
          val username = Option(request.getParam("username"))
          val passwd1 = Option(request.getParam("passwd1"))
          val passwd2 = Option(request.getParam("passwd2"))

          // 检查是否填写用户名,密码和重复密码
          (username, passwd1, passwd2) match {
            case (Some(x1), Some(x2), Some(x3)) =>
              // 若用户名和两个密码有一个为空则直接返回
              if (x1.isEmpty || x2.isEmpty || x3.isEmpty) {
                response.setContentString(
                  """
                    |{
                    |  "message": "username,passwd1 and passwd2 isn't null",
                    |  "status: "0"
                    |}
                    |""".stripMargin)
                response.status(Status.Unauthorized)
                // 直接返回响应
                return Future.value(response)
              }
          }

          // 若查询到重复用户名
          if (client.get("username", username.get).next()) {
            response.setContentString(
              """
                |{
                |  "message": "username already exists"
                |}
                |""".stripMargin)
            response.status(Status.Unauthorized)
          } else if (!passwd1.get.equals(passwd2.get)) {
            // 二次输入密码不一致
            response.setContentString(
              """
                |{
                |  "message": "passwd1 and passwd2 are inconsistent"
                |}
                |""".stripMargin)
            response.status(Status.Unauthorized)
          } else {
            client.put(
              List("username", "passwd"),
              List(username.get, passwd1.get))
            response.setContentString(
              """
                |{
                |  "message": "register success",
                |  "status": "1"
                |}
                |""".stripMargin)
            response.status(Status.Ok)
          }
        } else {
          // 请求方法错误
          response.setContentString(
            """
              |{
              |  "message": "please use POST method"
              |}
              |""".stripMargin)
          response.status(Status.MethodNotAllowed)
        }
      // 异常响应
      case _ =>
        response.setContentString(
          """
            |{
            |  "message": "not found"
            |}
            |""".stripMargin)
        response.status(Status.NotFound)
    }

    // 返回响应
    Future.value(response)
  }
}
