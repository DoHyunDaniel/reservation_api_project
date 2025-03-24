package com.reservation.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3UploaderService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * MultipartFile을 AWS S3 버킷에 업로드하는 메소드
     * - 파일 이름은 UUID + 원본 파일명으로 구성됩니다.
     * - 지정된 디렉토리(dirName) 하위에 저장됩니다.
     *
     * @param file 업로드할 MultipartFile
     * @param dirName S3 내 저장 디렉토리 이름 (예: "reviews")
     * @return 업로드된 파일의 전체 URL
     * @throws RuntimeException 업로드 중 IOException 발생 시
     */
    public String upload(MultipartFile file, String dirName) {
        // 고유 파일명 생성
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // S3에 파일 업로드
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucket,
                    fileName,
                    file.getInputStream(),
                    metadata
            );

            amazonS3.putObject(putObjectRequest);
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }

        // 업로드된 파일의 URL 반환
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    /**
     * S3 버킷에서 지정된 파일을 삭제하는 메소드
     *
     * @param fileName 삭제할 파일의 경로 (예: "reviews/uuid_filename.jpg")
     */
    public void delete(String fileName) {
        amazonS3.deleteObject(bucket, fileName);
    }
}
