package org.ruang.discover

import org.apache.zookeeper.{WatchedEvent, ZooKeeper}
import org.moon.http.RegisterCenter

class ZkServerDiscover(host: String, port: String) extends RegisterCenter {
  // zk根节点
  private val root = "/engineer"

  // zk客户端
  private val client = new ZooKeeper(
    s"$host:$port",
    5000,
    (event: WatchedEvent) => {
      println(s"rec: $event")
    })

  /**
   * 查找服务
   *
   * @param name 服务名称
   * @return (服务信息,服务状态)
   */
  override def discover(name: String): (String, String) = {
    val services = client.getChildren(root, false)
    services.forEach(x => {
      // 若找到对应名称的服务则返回服务名称
      if ("[a-zA-Z]+".r
        .findFirstMatchIn(x)
        .getOrElse("")
        .toString
        .equals(name)) {
        val server = new String(client.getData(s"$root/$x", false, null))
        return (name, server)
      }
    })
    ("", "")
  }

  override def register(name: String, host: String, port: String): Unit = {

  }
}
