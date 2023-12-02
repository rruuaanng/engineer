package org.ruang

import org.moon.common.config.Configure
import org.moon.http.CorsSetting
import org.ruang.remote.RemoteCommand
import org.ruang.rest.{Mail, TempNote, UserAuth}


object Main {

  private val config: Configure = Configure()

  // 注册服务
  //  private val zkClient = ZkRegisterCenter.connect(
  //    config.getSection("zkServer", "host"),
  //    config.getSection("zkServer", "port"))


  def main(args: Array[String]): Unit = {
    // 设置跨域
    val config = Configure()
    val servers = new CorsSetting()
      .add(new UserAuth, config.getSection(
        "userAuth",
        "port"))
      .add(new Mail, config.getSection(
        "mailServer",
        "port"
      ))
      .add(new TempNote, config.getSection(
        "tempNote",
        "port"))
      .add(new RemoteCommand, "4000")
      .apply()

    // 快速注册服务
    //    new FastZkRegister(zkClient, config)
    //      .add(List("userAuth", "tempNote"))
    // 启动服务
    while (true) {}
  }
}
