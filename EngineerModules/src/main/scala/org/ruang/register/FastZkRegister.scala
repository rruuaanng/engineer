package org.ruang.register

import org.moon.common.config.Configure

class FastZkRegister(zkClient: ZkRegisterCenter, config: Configure) {
  /**
   * 注册服务
   *
   * @param names 服务名称列表
   */
  def add(names: List[String]): Unit = {
    names.foreach(x => {
      zkClient.register(
        config.getSection(x, "name"),
        config.getSection(x, "host"),
        config.getSection(x, "port"))
    })
  }

}
