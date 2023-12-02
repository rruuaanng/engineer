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
          new MailCommand().execute(op)

        /**
         * 笔记相关命令
         */
        case Syntax.tempNoteCmd(op) =>
          new TempNoteCommand().execute(op)
          println(NetReq +> ("localhost:50020", "/note/gets"))
        // 未知命令
        case _ => println("unknown command")
      }
    }
  }
}
