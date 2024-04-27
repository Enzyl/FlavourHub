package pl.dlusk.business;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryClient {
    String uploadImageAndGetUrl(MultipartFile image) throws IOException;
    boolean deleteImage(String url) throws IOException;

}
