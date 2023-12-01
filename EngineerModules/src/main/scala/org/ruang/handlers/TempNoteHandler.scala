package org.ruang.handlers

import com.twitter.finagle.http.{Response, Status}
import io.circe.parser.parse
import org.moon.common.Json
import org.moon.common.config.JdbcConfig
import org.moon.store.ConnectorFactory

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset, ZonedDateTime}
import scala.collection.mutable.ListBuffer

object TempNoteHandler {

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

  def create(body: String, response: Response): Unit = {
    // 解析JSON
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
  }

  def update(id: String, body: String, response: Response): Unit = {
    // 解析JSON
    val json = parse(body)

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
        response.setContentString(Json.of(Map("message" -> "update success")))
        response.status(Status.Ok)
    }
  }

  def delete(id: String, response: Response): Unit = {
    client.delete("id", id)
    response.setContentString(Json.of(Map("message" -> "delete success")))
    response.status(Status.Ok)
  }

  def filter(id: String, response: Response): Unit = {
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
  }

  def gets(response: Response): Unit = {
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
  }

  //  def gets(): Unit = {
  //
  //  }
}
