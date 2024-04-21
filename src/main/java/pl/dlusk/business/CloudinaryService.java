package pl.dlusk.business;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to upload image because the file is empty.");
        }
        log.info("########## CloudinaryService: Attempting to upload an image");

        String publicId = UUID.randomUUID().toString();

        // Attempt to optimize image to fit within 1 MB
        Transformation transform = new Transformation()
                .width(500)  // Target width
                .height(500) // Target height
                .crop("limit") // Limit crop to keep aspect ratio
                .quality("auto:eco") // Auto adjust quality to reduce file size
                .fetchFormat("auto"); // Auto format based on the browser

        try {
            return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "image",
                    "public_id", publicId,
                    "transformation", transform
            ));
        } catch (Exception e) {
            log.error("Cloudinary upload failed", e);
            throw new IOException("Cloudinary upload failed", e);
        }
    }

    public boolean deleteImage(String url) throws IOException {
        if (url == null || url.isEmpty()) {
            log.error("Public ID is null or empty, cannot delete image.");
            return false;
        }

        log.info("########## CloudinaryService: Attempting to delete an image with url {}", parsePublicIdFromUrl(url));

        try {
            Map result = cloudinary.uploader().destroy(parsePublicIdFromUrl(url), ObjectUtils.emptyMap());
            log.info("Cloudinary delete response: {}", result);
            return result.get("result").equals("ok");
        } catch (Exception e) {
            log.error("Cloudinary delete failed for public ID {}: {}", url, e.getMessage());
            throw new IOException("Cloudinary delete failed for public ID " + parsePublicIdFromUrl(url), e);
        }
    }


    public String parsePublicIdFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            String[] parts = path.split("/");
            String filename = parts[parts.length - 1];
            String publicId = filename.substring(0, filename.lastIndexOf('.'));
            return publicId;
        } catch (URISyntaxException e) {
            log.error("Error parsing URL", e);
            return null;
        }
    }

}
