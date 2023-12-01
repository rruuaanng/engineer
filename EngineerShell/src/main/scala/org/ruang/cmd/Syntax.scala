package org.ruang.cmd

import scala.util.matching.Regex

/**
 * 命令语法 <br>
 *
 * 邮件 mail {send|receive}
 * 笔记 note {create|update|delete|get|gets}
 * 用户验证 user {login|register}
 */
object Syntax {
  // 跳过空格
  private val WS: Regex = """\s*""".r

  // 退出邮件内容编辑
  val quit: Regex = """(quit|exit)""".r

  // 笔记增删改查
  private val noteOp: Regex = """(create|delete|update|gets|get)""".r

  // 邮件接收/发送
  private val mailOp: Regex = """(send|receive)""".r

  // 邮件发送
  val mailSendCmd: Regex = s"""mail$WS$mailOp""".r

  // 笔记命令
  val tempNoteCmd: Regex = s"""note$WS$noteOp""".r
}
