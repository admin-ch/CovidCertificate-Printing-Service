package ch.admin.bag.covidcertificate.config;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageHandler;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
@Profile("!"+ProfileRegistry.PROFILE_SFTP_MOCK)
@Slf4j
public class SftpConfig {
    @Value("${bbl.sftp.host}")
    private String sftpServer;
    @Value("${bbl.sftp.port}")
    private int sftpPort;
    @Value("${bbl.sftp.user}")
    private String sftpUser;
    @Value("${bbl.sftp.password}")
    private String sftpPassword;
    @Value("${bbl.sftp.known_hosts}")
    private String knownHosts;

    private static final String KNOWN_HOSTS_FILENAME = "known_hosts";

    @PostConstruct
    private void storeKnownHost() throws IOException {
        try(var knownHostStream = new FileOutputStream(KNOWN_HOSTS_FILENAME)){
            var knownHostsDecoded = Base64.getDecoder().decode(knownHosts.getBytes());
            knownHostStream.write(knownHostsDecoded);
        }catch (IOException e) {
            log.error("Failed create known hosts file", e);
            throw e;
        }

    }

    @Bean
    public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory() {
        var factory = new DefaultSftpSessionFactory(true);
        factory.setKnownHostsResource(new FileSystemResource(KNOWN_HOSTS_FILENAME));
        factory.setHost(sftpServer);
        factory.setPort(sftpPort);
        factory.setUser(sftpUser);
        factory.setAllowUnknownKeys(false);
        factory.setUserInfo(new UserInfo() {
            @Override
            public String getPassphrase() {
                return null;
            }

            @Override
            public String getPassword() {
                return sftpPassword;
            }

            @Override
            public boolean promptPassword(String s) {
                return false;
            }

            @Override
            public boolean promptPassphrase(String s) {
                return false;
            }

            @Override
            public boolean promptYesNo(String s) {
                return false;
            }

            @Override
            public void showMessage(String s) {

            }
        });
        return new CachingSessionFactory<>(factory);
    }

    @Bean
    @ServiceActivator(inputChannel = "toSftpChannel")
    public MessageHandler handler() {
        var handler = new SftpMessageHandler(sftpSessionFactory());
        handler.setRemoteDirectoryExpressionString("headers['remote-target-dir']");

        return handler;
    }

    @MessagingGateway
    public interface PrintingServiceSftpGateway {

        @Gateway(requestChannel = "toSftpChannel")
        void sendToSftp(File file);

    }
}
