package com.spring.aws.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AwsS3ServiceImpl implements AwsS3Service{

    @Value("${upload.s3.localPath}")
    private String localPath;

    private final S3Client s3Client;

    public AwsS3ServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String uploadFile(MultipartFile multipartFile) throws IOException {
        try {
            String fileName = multipartFile.getOriginalFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket("s3-bucket-ivr")
                    .key(fileName)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));

            return "File uploaded successfully!";
        }catch (IOException e){
            throw new IOException("Error while uploading file to S3", e);
        }

    }

    @Override
    public String downloadFile(String fileName) throws IOException {

        if (!doesFileExist(fileName)) {
            throw new IOException(String.format("File %s not found!", fileName));
        }

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket("s3-bucket-ivr")
                .key(fileName)
                .build();

        ResponseInputStream<GetObjectResponse> objectResponse = s3Client.getObject(objectRequest);

        try (FileOutputStream fileOutputStream = new FileOutputStream(localPath + fileName)){

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = objectResponse.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

        }catch (IOException e){
            throw new IOException("Error while downloading file from S3", e);
        }

        return "File downloaded successfully!";
    }

    @Override
    public List<String> listFiles() throws IOException {
        try {
            ListObjectsRequest listObjectsRequest = ListObjectsRequest
                    .builder()
                    .bucket("s3-bucket-ivr")
                    .build();

            List<S3Object> s3Objects = s3Client.listObjects(listObjectsRequest).contents();
            List<String> fileNames = new ArrayList<>();

            for (S3Object s3Object : s3Objects) {
                fileNames.add(s3Object.key());
            }

            return fileNames;
        } catch (S3Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public String renameFile(String oldFileName, String newFileName) throws IOException {

        if (!doesFileExist(oldFileName)) {
            throw new IOException(String.format("File %s not found!", oldFileName));
        }

        try {
            CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                    .destinationBucket("s3-bucket-ivr")
                    .copySource("s3-bucket-ivr/" + oldFileName)
                    .destinationKey(newFileName)
                    .build();

            s3Client.copyObject(copyObjectRequest);

            this.deleteFile(oldFileName);

            return "File renamed successfully!";
        } catch (S3Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public String updateFile(MultipartFile multipartFile, String oldFileName) throws IOException {

        if (!doesFileExist(oldFileName)) {
            throw new IOException(String.format("File %s not found!", oldFileName));
        }

        try {
            String newFileName = multipartFile.getOriginalFilename();

            this.deleteFile(oldFileName);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket("s3-bucket-ivr")
                    .key(newFileName)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));

            return "File updated successfully!";
        }catch (IOException e){
            throw new IOException("Error while updating file to S3", e);
        }

    }

    @Override
    public String deleteFile(String fileName) throws IOException {

        if (!doesFileExist(fileName)) {
            throw new IOException(String.format("File %s not found!", fileName));
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket("s3-bucket-ivr")
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            return "File deleted successfully!";
        } catch (S3Exception e) {
            throw new IOException(e.getMessage());
        }
    }


    private boolean doesFileExist(String objectKey) {

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket("s3-bucket-ivr")
                    .key(objectKey)
                    .build();

            s3Client.headObject(headObjectRequest);

        }catch (S3Exception e){
            if (e.statusCode() == 404) {
                return false;
            }
        }

        return true;
    }
}
