package org.ruang

import org.moon.common.config.Configure
import org.moon.http.CorsSetting
import org.ruang.rest.{TempNote, UserAuth}


object Main {
  def main(args: Array[String]): Unit = {
    val config = Configure()
    // 启动服务
    new CorsSetting()
      .add(new UserAuth, config.getKey("userAuthPort"))
      .add(new TempNote, config.getKey("tempNotePort"))
      .start()
  }
}
