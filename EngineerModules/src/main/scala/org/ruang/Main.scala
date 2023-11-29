package org.ruang

import com.twitter.util.Await
import org.moon.common.config.Configure
import org.moon.http.CorsSetting
import org.ruang.register.{FastZkRegister, ZkRegisterCenter}
import org.ruang.rest.{TempNote, UserAuth}


object Main {

  private val config: Configure = Configure()

  // 注册服务
  private val zkClient = ZkRegisterCenter.connect(
    config.getSection("zkServer", "host"),
    config.getSection("zkServer", "port"))



  def main(args: Array[String]): Unit = {
    // 设置跨域
    val config = Configure()
    val servers = new CorsSetting()
      .add(new UserAuth, config.getSection(
        "userAuth",
        "port"))
      .add(new TempNote, config.getSection(
        "tempNote",
        "port"))
      .apply()

    // 快速注册服务
//    new FastZkRegister(zkClient, config)
//      .add(List("userAuth", "tempNote"))
    // 启动服务
    servers.foreach(x => Await.result(x))
  }
}
