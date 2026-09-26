package com.example.lotterysystem.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 邮件发送工具类
 * <p>基于 Spring {@link JavaMailSender} 发送简单文本邮件，常用于发送验证码等通知。</p>
 */
@Component
public class MailUtil {
    private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);
    /** 发件人地址，取自配置 spring.mail.username */
    @Value(value = "${spring.mail.username}")
    private String from;
    @Autowired
    private JavaMailSender mailSender;

    /**
     * 发邮件
     *
     * @param to:  目标邮箱地址
     * @param subject： 标题
     * @param context： 正文
     * @return
     */
    public Boolean sendSampleMail(String to, String subject, String context) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(context);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("向{}发送邮件失败！", to, e);
            return false;
        }
        return true;
    }
}