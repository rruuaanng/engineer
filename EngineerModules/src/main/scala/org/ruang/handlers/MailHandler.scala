package org.ruang.handlers

import com.twitter.util.Future

import java.util.Properties
import javax.mail.Message.RecipientType
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}

class MailHandler(account: String, passwd: String) {
  // 属性设置
  private case class MailSetting(title: String, text: String,
                                 filename: String, filepath: String,
                                 server: String, port: String,
                                 toAccount: String, protocolTyp: String)

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

    private var setting = MailSetting("", "", "", "", "", "", "", "")

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

    def setProtocolTyp(protocol: String): SendBuilder = {
      setting = setting.copy(protocolTyp = protocol)
      this
    }

    def send(props: Properties): Future[Unit] = Future {
      // 设置session连接属性
      props.put(s"mail.${setting.protocolTyp}.auth", "true")
      props.put(s"mail.${setting.protocolTyp}.starttls.enable", "true")
      props.put(s"mail.${setting.protocolTyp}.host", setting.server)
      props.put(s"mail.${setting.protocolTyp}.port", setting.port)

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

  /**
   * MailControl("your_mail_account", "your_mail_passwd")
   * .ReceiveBuilder
   * .setServer("pop-mail.outlook.com")
   * .setPort("995")
   * .setProtocolTyp("pop3")
   * .receive(new Properties())
   * .foreach(x => println(x.getSubject))
   */
  class ReceiveBuilder {
    private var setting = MailSetting("", "", "", "", "", "", "", "")

    def setServer(server: String): ReceiveBuilder = {
      setting = setting.copy(server = server)
      this
    }

    def setPort(port: String): ReceiveBuilder = {
      setting = setting.copy(port = port)
      this
    }

    def setProtocolTyp(protocol: String): ReceiveBuilder = {
      setting = setting.copy(protocolTyp = protocol)
      this
    }


    def receive(props: Properties): Future[Array[Message]] = Future {
      props.put(s"mail.${setting.protocolTyp}.host", setting.server)
      props.put(s"mail.${setting.protocolTyp}.port", setting.port)
      props.put(s"mail.${setting.protocolTyp}.ssl.enable", "true")

      val session = Session.getDefaultInstance(props);

      val store = session.getStore(s"${setting.protocolTyp}s");
      store.connect(
        setting.server,
        account,
        passwd)

      // 打开收件箱
      val inbox = store.getFolder("INBOX");
      inbox.open(Folder.READ_ONLY);

      // 获取邮件
      inbox.getMessages()
    }
  }

}

object MailHandler {
  /**
   * 创建邮件会话
   *
   * @param account 邮箱账号
   * @param passwd  邮箱密码
   * @return
   */

  def apply(account: String, passwd: String): MailHandler = {

    new MailHandler(account, passwd)
  }
}
