package org.ruang.register

import org.apache.zookeeper.{CreateMode, WatchedEvent, ZooDefs, ZooKeeper}
import org.moon.http.RegisterCenter

class ZkRegisterCenter(host: String, port: String) extends RegisterCenter {
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
   * 注册服务
   *
   * @param name 服务名称
   * @param host 主机号
   * @param port 端口
   * @param url  路由
   */
  def register(name: String, host: String, port: String): Unit = {
    val stat = Option(client.exists(root, false))
    if (stat.isDefined) {
      // 创建节点
      client.create(
        s"$root/$name",
        s"$host:$port".getBytes(),
        ZooDefs.Ids.READ_ACL_UNSAFE,
        CreateMode.EPHEMERAL_SEQUENTIAL)
    }
  }

  /**
   * 查找服务
   *
   * @param name 服务名称
   * @return (服务信息,服务状态)
   */
  def find(name: String): (String, Boolean) = {
    val services = client.getChildren(root, false)
    services.forEach(x => {
      // 若找到对应名称的服务则返回服务名称
      if ("[a-zA-Z]+".r
        .findFirstMatchIn(x)
        .getOrElse("")
        .toString
        .equals(name)) {
        val server = new String(client.getData(s"$root/$x", false, null))
        return (server, true)
      }
    })
    ("", false)
  }
}

object ZkRegisterCenter {
  /**
   * 创建zookeeper连接
   *
   * @param host zk主机名
   * @param port zk端口
   * @return
   */
  def connect(host: String, port: String): ZkRegisterCenter =
    new ZkRegisterCenter(host, port)
}


