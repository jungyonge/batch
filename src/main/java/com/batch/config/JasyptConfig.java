package com.batch.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;


@Configuration
public class JasyptConfig {

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();

        config.setPasswordSysPropertyName("jasypt.encryptor.password");
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);

        return encryptor;
    }

    public static String decrypt(String encryptedValue) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("jasypt.encryptor.password");
        return encryptor.decrypt(encryptedValue);
    }
}
