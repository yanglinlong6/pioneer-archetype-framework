package com.glsx.plat.mail.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MailUtils {

    @Resource
    private JavaMailSenderImpl mailSender;

    @Value("${spring.mail.username}")
    private String username;

    /**
     * 发送纯文本形式的email
     *
     * @param from    发件人邮箱
     * @param to      收件人邮箱
     * @param subject 邮件标题
     * @param content 邮件内容
     */
    public void sendTextMail(String from, String to, String subject, String content) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(content);
        log.info("发送邮件:" + msg.toString());
        mailSender.send(msg);
    }

    /**
     * 异步发送纯文本形式的email
     *
     * @param toEmail 收件人邮箱
     * @param subject 邮件标题
     * @param content 邮件内容
     */
    @Async
    public void sendTextMailAsync(String toEmail, String subject, String content) {
        sendTextMail(username, toEmail, subject, content);
    }

    /**
     * 同步发送纯文本形式的email
     *
     * @param toEmail 收件人邮箱
     * @param subject 邮件标题
     * @param content 邮件内容
     */
    public void sendTextMail(String toEmail, String subject, String content) {
        sendTextMail(username, toEmail, subject, content);
    }

    /**
     * 发送带有html的内容
     *
     * @param toEmail     收件人邮箱
     * @param subject     邮件标题
     * @param htmlContent 邮件内容
     */
    @Async
    public void sendHtmlMail(String toEmail, String subject, String htmlContent) throws MessagingException {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");
        helper.setFrom(username);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        log.info("发送邮件:" + msg.toString());
        mailSender.send(msg);
    }

    /**
     * 添加附件的email发送
     *
     * @param toEmail         收件人地址
     * @param subject         邮件标题
     * @param content         文本内容
     * @param attachmentFiles 附件信息 每个子项都是一个文件相关信息的map Map<String,String>: 1.filePath
     *                        2.fileName
     * @throws Exception 异常
     */
    @Async
    public void sendAttachmentMail(String toEmail, String subject, String content, List<Map<String, String>> attachmentFiles) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "utf-8");
        helper.setFrom(username);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(content);

        for (Map<String, String> file : attachmentFiles) {
            String filepath = file.get("filepath");
            String filename = file.get("filename");
            if (!StringUtils.isEmpty(filepath)) {
                if (filepath.contains("http") || filepath.contains("https")) {
                    String tmp = "/tmp/";
                    File tmpDir = new File(tmp);
                    if (!tmpDir.exists() && tmpDir.mkdir()) {

                    }
                    Snowflake snowflake = new Snowflake();//避免重复文件名问题
                    File attachmentFile = new File(tmp + snowflake.getGenerateDateTime(0L) + "_" + filename);
                    HttpUtil.downloadFileFromUrl(filepath, attachmentFile);
                    helper.addAttachment(filename, attachmentFile);
                } else {
                    FileSystemResource resource = new FileSystemResource(filepath);
                    if (resource.exists()) {// 是否存在资源
                        File attachmentFile = resource.getFile();
                        helper.addAttachment(filename, attachmentFile);
                    }
                }
            }
        }
        log.info("发送邮件:{},{},{},{}", username, toEmail, subject, content);
        mailSender.send(msg);
    }

}
