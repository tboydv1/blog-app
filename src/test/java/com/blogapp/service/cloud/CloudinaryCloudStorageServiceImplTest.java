package com.blogapp.service.cloud;

import com.blogapp.web.dto.PostDto;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.util.ByteSequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class CloudinaryCloudStorageServiceImplTest {

    @Autowired @Qualifier("cloudinary")
    CloudStorageService cloudStorageServiceImpl;

    @BeforeEach
    void setUp() {
    }

    @Test
    void uploadImageFileTest() {

        //define a file
        File file = new File("/home/oluwatobi/Pictures/images.jpeg");
        assertThat(file.exists()).isTrue();

        Map<Object, Object> params = new HashMap<>();
//        params.put("public_id", "blogapp/");
        params.put("public_id", "blogapp/post_file1");
        params.put("overwrite", "true");

        try {
            cloudStorageServiceImpl.uploadImage(file, params);
        }catch (IOException e){
            log.info("Error occurred --> {}", e.getMessage());
        }

    }

    @Test
    void uploadMultipartImageFileTest() throws IOException {

        PostDto postDto = new PostDto();
        postDto.setTitle("Test");
        postDto.setContent("Test");

        Path path = Paths.get("/home/oluwatobi/Pictures/images.jpeg");
        assertThat(path.toFile().exists());

        MultipartFile multipartFile = new MockMultipartFile("images.jpeg",
                "images.jpeg", "img/jpeg",
                Files.readAllBytes(path));

        log.info("Multipart Object created --> {}", multipartFile);
        assertThat(multipartFile).isNotNull();
        assertThat(multipartFile.isEmpty()).isFalse();
        postDto.setImageFile(multipartFile);

        log.info("File name --> {}", postDto.getImageFile().getOriginalFilename());
        cloudStorageServiceImpl.uploadImage(multipartFile, ObjectUtils.asMap(
           "public_id", "blogapp/"+extractFileName(Objects.requireNonNull(postDto.getImageFile().getOriginalFilename()))));
        assertThat(postDto.getImageFile().getOriginalFilename()).isEqualTo("images.jpeg");
    }

    private String extractFileName(String fileName){
        return fileName.split("\\.")[0];
    }
}