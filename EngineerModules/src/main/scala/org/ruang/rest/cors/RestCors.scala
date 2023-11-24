package org.ruang.rest.cors

import com.twitter.finagle.http.filter.Cors

object RestCors {
  // 用户验证跨域
  def userAuth: Cors.Policy = Cors.Policy(
    allowsOrigin = _ => Some("*"),
    allowsMethods = _ => Some(Seq("GET", "POST", "PUT", "DELETE")),
    allowsHeaders = _ => Some(Seq("Content-Type")))

  // 零食笔记跨域
  def tempNote: Cors.Policy = Cors.Policy(
    allowsOrigin = _ => Some("*"),
    allowsMethods = _ => Some(Seq("GET", "POST", "PUT", "DELETE")),
    allowsHeaders = _ => Some(Seq("Content-Type")))

}

