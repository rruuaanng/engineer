package org.ruang.rest

import com.twitter.finagle.http.path._
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.Future
import io.circe.parser._
import org.moon.common.Json
import org.moon.common.config.JdbcConfig
import org.moon.http.RestApi
import org.moon.store.ConnectorFactory

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset, ZonedDateTime}
import scala.collection.mutable.ListBuffer

/**
 * 临时笔记
 *
 * @author RUANG
 */
class TempNote extends RestApi {

  // JDBC配置
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
  private val client = ConnectorFactory.createJdbc("temp_note", dbConfig)

  override def apply(request: Request): Future[Response] = {
    val response = Response()
    response.setContentTypeJson()

    Path(request.path) match {
      /**
       * 创建笔记
       *
       * @note /note/put
       */
      case Root / "note" / "put" =>
        if (request.method.equals(Method.Post)) {
          // 获取body中的JSON
          val body = request.getContentString()
          val json = parse(body)

          json match {
            case Right(value) =>
              // 添加笔记的时间
              val time = ZonedDateTime.ofInstant(
                  Instant.now(), // 获取当前时间戳
                  ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) // 提取指定部分
              // 标题
              val title = value.hcursor
                .get[String]("title")
                .getOrElse("")
              // 内容
              val content = value.hcursor
                .get[String]("content")
                .getOrElse("")
              // 存储到数据库中
              client.put(
                List("id", "time", "title", "content"),
                List("0", time, title, content))
          }
          response.setContentString(Json.of(Map("message" -> "add note success")))
          response.status(Status.Ok)
        } else {
          // 请求错误
          response.setContentString(Json.of(Map("message" -> "need POST")))
          response.status(Status.MethodNotAllowed)
        }

      /**
       * 修改笔记
       *
       * @note /note/update?id=XX
       */
      case Root / "note" / "update" =>
        if (request.method.equals(Method.Put)) {
          // 获取请求参数
          val id = request.getParam("id")
          val body = request.getContentString()
          val json = parse(body)

          //
          json match {
            case Right(value) =>
              // 更新上次修改的时间
              val time = ZonedDateTime.ofInstant(
                  Instant.now(),
                  ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
              // 获取标题
              val title = value.hcursor
                .get[String]("title")
                .getOrElse("")
              // 获取内容
              val content = value.hcursor
                .get[String]("content")
                .getOrElse("")
              // 修改笔记
              client.update(
                "id", id,
                List("time", "title", "content"),
                List(time, title, content))
          }

        } else {
          // 请求错误
          response.setContentString(Json.of(Map("message" -> "need PUT")))
          response.status(Status.MethodNotAllowed)
        }

      /**
       * 删除笔记
       *
       * @note /note/delete?id=XX
       */
      case Root / "note" / "delete" =>
        if (request.method.equals(Method.Delete)) {
          val id = request.getParam("id")
          client.delete("id", id)
          response.setContentString(Json.of(Map("message" -> "delete success")))
          response.status(Status.Ok)
        } else {
          // 请求错误
          response.setContentString(Json.of(Map("message" -> "need DELETE")))
          response.status(Status.MethodNotAllowed)
        }

      /**
       * 获取指定ID的笔记
       *
       * @note /note/filter?id=XX
       */
      case Root / "note" / "filter" =>
        if (request.method.equals(Method.Get)) {
          // 获取参数和对应ID的数据
          val id = request.getParam("id")
          val note = client.get("id", id)

          // 若查询到则返回指定信息
          if (note.next()) {
            // 返回数据
            response.setContentString(Json.of(Map(
              "id" -> note.getString("id"),
              "time" -> note.getString("time"),
              "title" -> note.getString("title"),
              "content" -> note.getString("content")
            )))
            response.status(Status.Ok)
          } else {
            // 否则返回不存在
            response.setContentString(Json.of(Map("message" -> "not found note id with content")))
            response.status(Status.NotFound)
          }
        } else {
          // 请求错误
          response.setContentString(Json.of(Map("message" -> "need GET")))
          response.status(Status.MethodNotAllowed)
        }

      /**
       * 获取所有笔记
       *
       * @note /note/gets
       */
      case Root / "note" / "gets" =>
        if (request.method.equals(Method.Get)) {
          val notes = client.gets(List("id", "time", "title", "content"))
          val json = ListBuffer[String]()

          // 封装JSON
          while (notes.next()) {
            json.append(Json.of(Map(
              "id" -> notes.getString("id"),
              "time" -> notes.getString("time"),
              "title" -> notes.getString("title"),
              "content" -> notes.getString("content")
            )))
          }
          // 返回数据
          response.setContentString(s"[${json.mkString(",")}]")
          response.status(Status.Ok)
        } else {
          // 请求错误
          response.setContentString(Json.of(Map("message" -> "need GET")))
          response.status(Status.MethodNotAllowed)
        }
      // 异常访问
      case _ =>
        response.setContentString(Json.of(Map("message" -> "not found")))
        response.status(Status.NotFound)
    }

    Future.value(response)
  }
}
