package com.batch.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;


@Configuration
public class JasyptConfig {

    private static final String password = System.getProperty("jasypt.encryptor.password");

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setProviderName("SunJCE");
        encryptor.setPoolSize(2);
        encryptor.setPassword(password);
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        return encryptor;
    }

    public static String decrypt(String encryptedValue) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        String value = encryptedValue.replace("ENC(", "").replace(")", "");
        encryptor.setProviderName("SunJCE");
        encryptor.setPoolSize(2);
        encryptor.setPassword(password);
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        return encryptor.decrypt(value);
    }
}
