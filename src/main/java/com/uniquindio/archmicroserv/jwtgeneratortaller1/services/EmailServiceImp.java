package com.uniquindio.archmicroserv.jwtgeneratortaller1.services;


import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.EmailDTO;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImp{

    @Value("${simplejavamail.smtp.host}")
    private String SMTP_HOST;

    @Value("${simplejavamail.smtp.port}")
    private int SMTP_PORT;

    @Value("${simplejavamail.smtp.username}")
    private String SMTP_USERNAME;

    @Value("${simplejavamail.smtp.password}")
    private String SMTP_PASSWORD;


    @Async
    public void sendEmail(EmailDTO emailDTO) throws Exception {
        Email email = EmailBuilder.startingBlank()
                .from(SMTP_USERNAME)
                .to(emailDTO.receiver())
                .withSubject(emailDTO.subject())
                //This plain text could be replaced with "withHTMLText"
                .withPlainText(emailDTO.body())
                .buildEmail();


        try (Mailer mailer = MailerBuilder
                .withSMTPServer(SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withDebugLogging(true)
                .buildMailer()) {


            mailer.sendMail(email);
        }
    }

    @Async
    public void sendEmailHtmlWithAttachment(EmailDTO emailDTO, byte[] qrCodeImage, String qrCodeContentId) throws Exception {
        Email email = EmailBuilder.startingBlank()
                .from(SMTP_USERNAME)
                .to(emailDTO.receiver())
                .withSubject(emailDTO.subject())
                .withHTMLText(emailDTO.body())
                .withEmbeddedImage(qrCodeContentId, qrCodeImage, "image/png") // Adjuntar la imagen con el CID
                .buildEmail();

        try (Mailer mailer = MailerBuilder
                .withSMTPServer(SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withDebugLogging(true)
                .buildMailer()) {

            mailer.sendMail(email);
        }
    }

}