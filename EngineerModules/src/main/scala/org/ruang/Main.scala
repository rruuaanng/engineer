package org.ruang

import com.twitter.finagle.Http
import com.twitter.finagle.http.filter.Cors
import com.twitter.util.Await
import org.moon.config.Configure
import org.ruang.rest.cors.RestCors
import org.ruang.rest.{TempNote, UserAuth}


object Main {
  def main(args: Array[String]): Unit = {
    val config = Configure()

    // 用户验证
    val userAuthCors = new Cors.HttpFilter(RestCors.userAuth)
    val userAuth = Http.serve(
      s":${config.getKey("userAuthPort")}",
      userAuthCors.andThen(new UserAuth))

    // 临时笔记
    val tempNoteCors = new Cors.HttpFilter(RestCors.tempNote)
    val tempNote = Http.serve(
      s":${config.getKey("tempNotePort")}",
      tempNoteCors.andThen(new TempNote))

    // 阻塞运行
    Await.ready(userAuth)
    Await.ready(tempNote)
  }
}
