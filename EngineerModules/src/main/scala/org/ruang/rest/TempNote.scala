package org.ruang.rest

import com.twitter.finagle.http.path._
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.Future
import org.moon.common.Json
import org.moon.http.RestAPI
import org.ruang.handlers.TempNoteHandler

/**
 * 临时笔记
 *
 * @author RUANG
 */
class TempNote extends RestAPI {


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
          // 创建一个笔记
          TempNoteHandler
            .create(body, response)
            .onSuccess(_ => println("create success"))
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
          // 修改指定ID的笔记
          TempNoteHandler
            .update(id, body, response)
            .onSuccess(_ => println("update success"))
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
          // 删除指定ID的笔记
          TempNoteHandler
            .delete(id, response)
            .onSuccess(_ => println("delete success"))
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
          // 获取指定的数据
          TempNoteHandler
            .filter(id, response)
            .onSuccess(_ => println("filter success"))
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
          // 获取所有数据
          TempNoteHandler
            .gets(response)
            .onSuccess(_ => println("gets success"))

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
