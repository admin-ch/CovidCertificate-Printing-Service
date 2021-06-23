package ch.admin.bag.covidcertificate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.MessageHandler;

import java.io.File;

@Configuration
@Profile(ProfileRegistry.PROFILE_SFTP_MOCK)
public class SftpMockConfig {

    @Bean
    @ServiceActivator(inputChannel = "toSftpChannel")
    public MessageHandler handler() {
        return new LoggingHandler(LoggingHandler.Level.INFO);
    }

    @MessagingGateway
    public interface PrintingServiceSftpGateway {

        @Gateway(requestChannel = "toSftpChannel")
        void sendToSftp(File file);

    }
}
