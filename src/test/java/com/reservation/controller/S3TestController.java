package com.reservation.controller;

import com.reservation.service.S3UploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
public class S3TestController {

    private final S3UploaderService s3UploaderService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadTest(@RequestParam("file") MultipartFile file) {
        try {
            String url = s3UploaderService.upload(file, "test-folder");
            return ResponseEntity.ok("Uploaded URL: " + url);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("S3 Upload Failed: " + e.getMessage());
        }
    }
}
