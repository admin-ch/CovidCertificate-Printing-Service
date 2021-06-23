package ch.admin.bag.covidcertificate.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * use this class to document your spring profile or document it in confluence. but... document it!
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileRegistry {
    /**
     * if activated, use a pkcs12 based mock otherwise use hsm-based security
     */
    public static final String PROFILE_SFTP_MOCK = "sftp-mock";
}

