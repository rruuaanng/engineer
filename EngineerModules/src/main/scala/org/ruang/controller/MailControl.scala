package org.ruang.controller

import java.util.Properties
import javax.mail.Message.RecipientType
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Authenticator, PasswordAuthentication, Session, Transport}

class MailControl(account: String, passwd: String) {

  def SendBuilder: SendBuilder = new SendBuilder

  /**
   * MailControl(
   * "your_mail_account",
   * "your_passwd")
   * .SendBuilder
   * .setTitle("title")
   * .setText("content")
   * .setSmtpServer("target_mail_server")
   * .setSmtpPort("target_mail_port")
   * .setToAccount("receive_account")
   * .send()
   */
  class SendBuilder {
    // 属性设置
    private case class MailSetting(title: String, text: String,
                                   filename: String, filepath: String,
                                   server: String, port: String,
                                   toAccount: String)

    private var setting = MailSetting("", "", "", "", "", "", "")

    def setTitle(title: String): SendBuilder = {
      setting = setting.copy(title = title)
      this
    }

    def setText(text: String): SendBuilder = {
      setting = setting.copy(text = text)
      this
    }

    def setSmtpServer(server: String): SendBuilder = {
      setting = setting.copy(server = server)
      this
    }

    def setSmtpPort(port: String): SendBuilder = {
      setting = setting.copy(port = port)
      this
    }

    def setFileName(filename: String): SendBuilder = {
      setting = setting.copy(filename = filename)
      this
    }

    def setFilePath(filepath: String): SendBuilder = {
      setting = setting.copy(filepath = filepath)
      this
    }

    def setToAccount(account: String): SendBuilder = {
      setting = setting.copy(toAccount = account)
      this
    }

    def send(): Unit = {
      // 设置session连接属性
      val props = new Properties()
      props.put("mail.smtp.auth", "true")
      props.put("mail.smtp.starttls.enable", "true")
      props.put("mail.smtp.host", setting.server)
      props.put("mail.smtp.port", setting.port)

      // 邮件会话
      val session: Session = Session.getInstance(props, new Authenticator() {
        override protected def getPasswordAuthentication =
          new PasswordAuthentication(account, passwd)
      })

      // 设置邮件内容
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(account))
      message.setRecipients(
        RecipientType.TO,
        new InternetAddress(setting.toAccount).getAddress)
      message.setSubject(setting.title)
      message.setText(setting.text)

      // 是否设置附件
      if (!setting.filename.equals("")
        || !setting.filepath.equals("")) {
        // 设置附件
        message.setFileName(setting.filename)
      }

      // 发送邮件
      Transport.send(message)
    }
  }

  def ReceiveBuilder: ReceiveBuilder = new ReceiveBuilder

  class ReceiveBuilder {

  }

}

object MailControl {
  /**
   * 创建邮件会话
   *
   * @param account 邮箱账号
   * @param passwd  邮箱密码
   * @return
   */

  def apply(account: String, passwd: String): MailControl = {

    new MailControl(account, passwd)
  }
}
