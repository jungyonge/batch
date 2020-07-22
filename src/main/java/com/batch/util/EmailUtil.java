package com.batch.util;

import com.batch.mapper.CommonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Slf4j
@Component
public class EmailUtil {

    private static final String SMTP_HOST_NAME = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";
    private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

    @Value("${excel.output-path}")
    private String excelOutputPath;

    public void sendSSLMessage(String recipient, List excelDataList, String from) throws MessagingException, UnsupportedEncodingException {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        DateFormat df = new SimpleDateFormat("yyyy년 MM월 dd일");

        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.auth", "true");
        props.put("mail.debug", "false");
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.socketFactory.port", SMTP_PORT);
        props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.put("mail.smtp.socketFactory.fallback", "false");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("jungyongee@gmail.com", "ghzkrp153");
                    }
                }
        );

        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);

        InternetAddress addressTo = new InternetAddress(recipient);

        msg.setRecipient(Message.RecipientType.TO, addressTo);

        // Setting the Subject and Content Type
        msg.setSubject(df.format(cal.getTime()) + " 스포츠 데이터 입니다.");
        BodyPart messageBodyPart = new MimeBodyPart();

        // Fill the message
        messageBodyPart.setText(df.format(cal.getTime()) + " 스포츠 데이터 입니다.");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Part two is attachment
        DateFormat df1 = new SimpleDateFormat("yyyyMMdd");
        for(int i = 0 ; i < excelDataList.size() ; i++){
            messageBodyPart = new MimeBodyPart();
            HashMap excelData = (HashMap) excelDataList.get(i);
            String fileName = excelData.get("SPORT") +"_"+ df1.format(cal.getTime()) + ".xls";
            File file = new File(excelOutputPath + fileName);
            FileDataSource fds = new FileDataSource(file);
            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.setFileName(MimeUtility.encodeText(fds.getName(),"UTF-8","B"));
            multipart.addBodyPart(messageBodyPart);
        }

        // Put parts in message
        msg.setContent(multipart);

        // Send the message
        Transport.send(msg);
        log.info("Send Email : " + recipient);

    }

}
