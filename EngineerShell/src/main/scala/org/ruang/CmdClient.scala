package org.ruang

import org.moon.http.NetReq
import org.ruang.cmd.{MailCommand, Syntax, TempNoteCommand}

import scala.io.StdIn

object CmdClient {
  def main(args: Array[String]): Unit = {
    while (true) {
      // 读取用户输入的命令
      print("engineer> ")
      val line = StdIn.readLine()
      // TODO
      // 解析命令
      line match {
        // 退出shell
        case Syntax.quit(_) =>
          System.exit(0)

        /**
         * 邮件命令
         */
        case Syntax.mailSendCmd(op) =>
          val token = new MailCommand().execute(op)
          NetReq +> ("localhost:4000", "/command", "POST", token)

        /**
         * 笔记相关命令
         */
        case Syntax.tempNoteCmd(op) =>
          new TempNoteCommand().execute(op)

        // 未知命令
        case _ => println("unknown command")
      }
    }
  }
}
