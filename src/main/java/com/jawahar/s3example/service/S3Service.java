package com.jawahar.s3example.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadObject(MultipartFile multipartFile) {
        File fileObject = convertMultipartFileToFile(multipartFile);
        String fileName = multipartFile.getOriginalFilename() + "_" + System.currentTimeMillis();
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObject));
        fileObject.delete();
        return fileName + " " + "to s3 successfully";
    }

    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
        try {
            return IOUtils.toByteArray(s3ObjectInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, fileName);
        return fileName + " " + "deleted";
    }

    public List<String> listAllFiles() {
        ListObjectsV2Result result = null;
        try {
            ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(10);

            result = s3Client.listObjectsV2(req);

            return result.getObjectSummaries().stream().map(object -> object.getKey()).collect(Collectors.toList());


        } catch (
                AmazonServiceException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) {
        File convertedFile = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream fileOutputStream = new FileOutputStream(convertedFile);) {
            fileOutputStream.write(multipartFile.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return convertedFile;
    }
}
