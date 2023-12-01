package org.ruang.cmd

class TempNoteCommand
  extends Command with Display[String] {
  override def execute(op: String): String = {
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
