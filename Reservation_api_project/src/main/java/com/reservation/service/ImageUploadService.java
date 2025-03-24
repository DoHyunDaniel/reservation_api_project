package com.reservation.service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageUploadService {
    private final String uploadDir = "/uploads/reviews"; // 루트 디렉토리 기준 경로

    public String uploadImage(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalFilename;
            File saveFile = new File(uploadDir, fileName);

            // 디렉토리 없으면 생성
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }

            file.transferTo(saveFile);

            // 프론트가 접근 가능한 URL 반환 (경로 설정에 따라 다를 수 있음)
            return "/images/reviews/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }
}
