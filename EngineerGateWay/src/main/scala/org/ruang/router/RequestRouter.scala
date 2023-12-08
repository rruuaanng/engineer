package org.ruang.router

import com.twitter.finagle.http.path._
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import org.moon.common.Json
import org.moon.common.config.Configure
import org.moon.http.{APIGateway, ForwardRequest}
import org.ruang.discover.ZkServerDiscover

import scala.collection.mutable.ListBuffer

/**
 * API网关
 */
class RequestRouter extends APIGateway {
  override def apply(request: Request): Future[Response] = {
    val response = Response()
    response.setContentTypeJson()

    // 获取服务列表
    val config = Configure()
    val list = config.getList("servers")
    // ListBuffer(("userAuth", "localhost:50010"), ("tempNote", "localhost:50020"))
    val servers = ListBuffer[(String, String)]()

    // 服务发现
    val zkCenter = new ZkServerDiscover("192.168.10.137", "2181")
    list.forEach(x => {
      val name = x.unwrapped().toString
      // 将服务状态和路由存储到servers中
      servers.append(zkCenter.discover(name))
    })

    // 请求转发
    Path(request.path) match {
      // 将请求转发到后端 login RESTapi
      case Root / "login" =>
        forwardRequest(servers.find(_._1 == "userAuth"), request, response)
      // 转发请求到后端 register RESTapi
      case Root / "register" =>
        forwardRequest(servers.find(_._1 == "userAuth"), request, response)
      // 转发请求到后端 tempNote RESTapi
      case Root / "note" / _ =>
        forwardRequest(servers.find(_._1 == "tempNote"), request, response)
      // 异常访问
      case _ =>
        response.setContentString(Json.of(Map("message" -> "not found")))
        response.status(Status.NotFound)
    }
    Future.value(response)
  }

  /**
   * 转发请求
   *
   * @param server   服务元组(服务名称, 服务地址)
   * @param request  请求
   * @param response 响应
   */

  private def forwardRequest(server: Option[(String, String)],
                             request: Request, response: Response): Unit = {
    server.foreach { case (_, hostPort) =>
      // 获取主机名和端口
      val Array(host, port) = hostPort.split(":")
      // 转发到对应服务
      new ForwardRequest()
        .+>(host, port, request)
        .process { x =>
          response.setContentString(x.getContentString())
        }
        .close
    }
  }
}
