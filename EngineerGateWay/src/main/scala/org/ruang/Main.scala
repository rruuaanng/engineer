package org.ruang

import com.twitter.finagle.http.filter.Cors
import com.twitter.util.Await
import org.moon.http.CorsSetting
import org.ruang.router.RequestRouter

object Main {
  private def setting: Cors.Policy = Cors.Policy(
    allowsOrigin = _ => Some("*"),
    allowsMethods = _ => Some(Seq("GET", "POST", "PUT", "DELETE")),
    allowsHeaders = _ => Some(Seq("Content-Type")))

  def main(args: Array[String]): Unit = {
    // 设置跨域
    val servers = new CorsSetting()
      .add(new RequestRouter, "8080")
      .apply()
    // 启动服务
    servers.foreach(x => Await.result(x))
  }
}
