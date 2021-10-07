package com.nowcoder.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {

    //    打印日志信息需要的类
    public static Logger logger = LoggerFactory.getLogger(MailClient.class);

    /**
     * 发送邮件需要的类
     */
    @Autowired
    JavaMailSender mailSender;

    //    发送者，固定就是配置类中配置的那个用户
    @Value("${spring.mail.username}")
    String from;

    /**
     * 发送邮件
     * @param to 邮件接收者
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public void sendMail(String to, String subject, String content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
//            helper类似于一个框架，相关信息都放在这里面
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);//true表示是否接收html格式
//            最后不要忘记将该邮件发送出去
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送失败：" + e.getMessage());
        }
    }

}
