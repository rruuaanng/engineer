package org.ruang.controller

import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.{JWSAlgorithm, JWSHeader, JWSObject, Payload}
import com.nimbusds.jwt.JWTClaimsSet
import com.twitter.finagle.http.{Response, Status}
import org.moon.common.Json
import org.moon.common.config.{Configure, JdbcConfig}
import org.moon.store.ConnectorFactory

import java.util.Date

/**
 * 用户验证请求处理
 */
object UserAuthControl {

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

  /**
   * 验证账户
   *
   * @param username 用户名
   * @param passwd   密码
   * @param time     请求时间
   * @param response 响应
   */
  def verifyAccount(username: String, passwd: String,
                    time: Date, response: Response): Unit = {
    // 获取用户数据
    val user = client.get("username", username)

    // 获取到数据
    if (user.next()) {
      // 验证账号和密码
      if (user.getString("username").equals(username)
        && user.getString("passwd").equals(passwd)) {
        // 登录成功
        // JWT主体
        val body = jwtBody
          .subject(username)
          .issueTime(time)
          .expirationTime(new Date(time.getTime + 3600000)) // 一小时后过期
          .build()
        val jwt = new JWSObject(jwtHeader, new Payload(body.toJSONObject))

        // 签名并返回JWT令牌
        jwt.sign(key)
        response.setContentString(Json.of(Map(
          "message" -> "login success",
          "status" -> "1",
          "token" -> jwt.serialize()
        )))
        response.status(Status.Ok)
      } else {
        // 密码错误
        response.setContentString(Json.of(Map("message" -> "passwd error")))
        response.status(Status.Unauthorized)
      }
    } else {
      // 用户不存在
      response.setContentString(Json.of(Map("message" -> "account don't exists")))
      response.status(Status.Unauthorized)
    }
  }

  /**
   * 检查用户是否已注册
   *
   * @param username 用户名
   * @param passwd1  密码
   * @param passwd2  确认密码
   * @param response 响应
   */
  def checkAccountExists(username: String,
                         passwd1: String, passwd2: String, response: Response): Unit = {
    if (client.get("username", username).next()) {
      response.setContentString(Json.of(Map("message" -> "username already exists")))
      response.status(Status.Unauthorized)
    } else if (!passwd1.equals(passwd2)) {
      // 二次输入密码不一致
      response.setContentString(Json.of(Map("message" -> "passwd1 and passwd2 are inconsistent")))
      response.status(Status.Unauthorized)
    } else {
      client.put(
        List("username", "passwd"),
        List(username, passwd1))
      response.setContentString(Json.of(Map(
        "message" -> "register success",
        "status" -> "1"
      )))
      response.status(Status.Ok)
    }
  }

  def loginParamIsNull(username: Option[String],
                       passwd: Option[String],
                       response: Response): Boolean = {
    (username, passwd) match {
      case (Some(x1), Some(x2)) =>
        if (x1.isEmpty || x2.isEmpty) {
          response.setContentString(Json.of(Map(
            "message" -> "username or passwd isn't null",
            "status" -> "0"
          )))
          response.status(Status.Unauthorized)
          return true
        }
    }
    false
  }

  def registerParamIsNull(username: Option[String],
                          passwd1: Option[String],
                          passwd2: Option[String],
                          response: Response): Boolean = {
    (username, passwd1, passwd2) match {
      case (Some(x1), Some(x2), Some(x3)) =>
        // 若用户名和两个密码有一个为空则直接返回
        if (x1.isEmpty || x2.isEmpty || x3.isEmpty) {
          response.setContentString(Json.of(Map(
            "message" -> "username,passwd1 and passwd2 isn't null",
            "status" -> "0"
          )))
          response.status(Status.Unauthorized)
          return true
        }
    }
    false
  }
}
