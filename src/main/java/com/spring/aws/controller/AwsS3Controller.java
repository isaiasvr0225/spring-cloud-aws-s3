package com.spring.aws.controller;

import com.spring.aws.service.AwsS3Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class AwsS3Controller {

    private final AwsS3Service awsS3Service;

    public AwsS3Controller(AwsS3Service awsS3Service) {
        this.awsS3Service = awsS3Service;
    }

    @GetMapping("/list")
    public List<String> listFiles() throws IOException {
        return awsS3Service.listFiles();
    }

    @GetMapping("/download/{fileName}")
    public String downloadFile(@PathVariable("fileName") String fileName) throws IOException {
        return awsS3Service.downloadFile(fileName);
    }
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("multipartFile") MultipartFile multipartFile) throws IOException {
        return awsS3Service.uploadFile(multipartFile);
    }

    @PutMapping("/rename/{oldFileName}/{newFileName}")
    public String renameFile(@PathVariable("oldFileName") String oldFileName,
                             @PathVariable("newFileName") String newFileName) throws IOException {
        return awsS3Service.renameFile(oldFileName, newFileName);
    }

    @PutMapping("/update/{oldFileName}")
    public String updateFile(@RequestParam("multipartFile") MultipartFile multipartFile,
                             @PathVariable("oldFileName") String oldFileName) throws IOException {
        return awsS3Service.updateFile(multipartFile, oldFileName);
    }

    @DeleteMapping("/delete/{fileName}")
    public String deleteFile(@PathVariable("fileName") String fileName) throws IOException {
        return awsS3Service.deleteFile(fileName);
    }

}
