package org.ruang

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.moon.http.RestAPI

object Main {
  def main(args: Array[String]): Unit = {
    val api = new RestAPI {
      override def apply(request: Request): Future[Response] = {
        val response = Response()
        response.setContentString("hello world")
        Future.value(response)
      }
    }
    Http.serve(":8080", api)
    while (true) {}
    // 设置跨域
    //    val servers = new CorsSetting()
    //      .add(new RequestRouter, "8080")
    //      .apply()
    //    // 启动服务
    //    servers.foreach(x => Await.result(x))
  }
}
