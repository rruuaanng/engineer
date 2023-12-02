package org.ruang.cmd

class TempNoteCommand
  extends Command with Display[String] {
  override def execute(op: String): String = {
    // Modules项目要编写新的处理模块
    // 以实现命令方法执行本地化
    // TODO
    op match {
      case "create" => println("create")
      case "delete" => println("delete")
      case "update" => println("update")
      case "get" => println("get")
      case "gets" => println("gets")
    }
    ""
  }

  override def show(text: String): Unit = {

  }
}
