package com.reservation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * AWS S3 설정 클래스
 * - S3 클라이언트를 Bean으로 등록하여 서비스에서 주입 받을 수 있도록 구성
 * - application.yml 또는 properties에 설정된 access-key, secret-key, region 사용
 */
@Configuration
@EnableConfigurationProperties
public class S3Config {

    /**
     * AWS S3의 리전(region) 값
     * 예: ap-northeast-2 (서울)
     */
    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * AmazonS3 Bean 등록
     * - AWS 자격 증명(accessKey, secretKey)을 사용하여 인증된 S3 클라이언트 생성
     *
     * @param accessKey AWS Access Key
     * @param secretKey AWS Secret Key
     * @return 인증된 AmazonS3 클라이언트 객체
     */
    @Bean
    public AmazonS3 amazonS3(@Value("${cloud.aws.credentials.access-key}") String accessKey,
                             @Value("${cloud.aws.credentials.secret-key}") String secretKey) {

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }
}
