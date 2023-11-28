package org.ruang.rest

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.moon.http.RestApi

class Mail extends RestApi {
  override def apply(request: Request): Future[Response] = {
    val response = Response()
    response.setContentTypeJson()


    Future.value(response)
  }
}
