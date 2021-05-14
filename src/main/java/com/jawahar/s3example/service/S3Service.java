package com.jawahar.s3example.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mustache.MustacheResourceTemplateLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class S3Service {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadObject(MultipartFile multipartFile){
        File fileObject = convertMultipartFileToFile(multipartFile);
        String fileName = multipartFile.getOriginalFilename()+"_"+System.currentTimeMillis();
        s3Client.putObject(new PutObjectRequest(bucketName, fileName,fileObject ));
        fileObject.delete();
        return fileName + " " + "to s3 successfully";
    }

    public byte[] downloadFile(String fileName){
       S3Object s3Object= s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
        try {
            return IOUtils.toByteArray(s3ObjectInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String deleteFile(String fileName){
        s3Client.deleteObject(bucketName, fileName);
        return fileName + " "+ "deleted";
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile){
        File convertedFile = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream fileOutputStream = new FileOutputStream(convertedFile);) {
            fileOutputStream.write(multipartFile.getBytes());

        }  catch (IOException e) {
             e.printStackTrace();
        }
        return convertedFile;
    }
}
