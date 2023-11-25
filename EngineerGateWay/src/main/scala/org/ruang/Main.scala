package org.ruang

import com.twitter.finagle.Http
import com.twitter.util.Await
import org.ruang.router.RequestRouter

object Main {
  def main(args: Array[String]): Unit = {
    val requestRouter = Http.serve(":8080", new RequestRouter)
    Await.ready(requestRouter)
  }
}
