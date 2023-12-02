package org.ruang.cmd

import org.moon.common.Json

import scala.io.StdIn

class MailCommand
  extends Command with Display[String] {

  override def show(text: String): Unit = {

  }

  override def execute(op: String): String = {
    op match {
      case "send" =>
        // 读取输入
        print("please input your mail title: ")
        val title = StdIn.readLine()
        print("please input your mail text: ")
        val text = StdIn.readLine()
        print("please input filepath of annex: ")
        val filepath = StdIn.readLine()
        // 封装为Token
        Json.of(Map(
          "op" -> op,
          "title" -> title,
          "text" -> text,
          "filepath" -> filepath
        ))
    }
  }
}
