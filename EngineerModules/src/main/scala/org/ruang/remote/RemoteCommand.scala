package org.ruang.remote

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.moon.http.RestApi

/**
 * 接收远程发送的命令
 */
class RemoteCommand extends RestApi {
  /*
  shell将接收到的命令封装为JSON，通过POST请求发送到modules（本文件）
  然后对POST包中的命令进行解析并调用对应接口，将结果已字符串的形式返回到shell
   */
  override def apply(request: Request): Future[Response] = {
    val response = Response()
    response.setContentTypeJson()

    Future.value(response)
  }
}
