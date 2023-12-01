package org.ruang.cmd

trait Command {
  def execute(op: String): String
}
