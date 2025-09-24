
package com.nextinnomind.campusnestbackend.service;

import com.nextinnomind.campusnestbackend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    @Value("${app.file-storage.base-url}")
    private String baseUrl;

    private Path fileStorageLocation;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage directory created at: {}", this.fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String storeFile(MultipartFile file, String category) {
        validateFile(file);

        String fileName = generateFileName(file, category);
        Path categoryPath = this.fileStorageLocation.resolve(category);

        try {
            // Create category directory if it doesn't exist
            Files.createDirectories(categoryPath);

            Path targetLocation = categoryPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = baseUrl + "/" + category + "/" + fileName;

            log.info("File stored successfully: {}", fileName);
            return fileUrl;

        } catch (IOException e) {
            log.error("Failed to store file: {}", fileName, e);
            throw new BadRequestException("Could not store file: " + fileName);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            // Extract file path from URL
            String relativePath = fileUrl.replace(baseUrl + "/", "");
            Path filePath = this.fileStorageLocation.resolve(relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", relativePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }

    public boolean fileExists(String fileName, String category) {
        Path filePath = this.fileStorageLocation.resolve(category).resolve(fileName);
        return Files.exists(filePath);
    }

    public Path getFilePath(String fileName, String category) {
        return this.fileStorageLocation.resolve(category).resolve(fileName);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BadRequestException("File type could not be determined");
        }

        if (!ALLOWED_IMAGE_TYPES.contains(contentType) && !ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
            throw new BadRequestException("File type not allowed: " + contentType);
        }

        // Check for malicious file extensions
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new BadRequestException("Invalid file name: " + fileName);
        }
    }

    private String generateFileName(MultipartFile file, String category) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return category + "_" + timestamp + "_" + uuid + fileExtension;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public List<String> storeMultipleFiles(List<MultipartFile> files, String category) {
        return files.stream()
                .map(file -> storeFile(file, category))
                .toList();
    }

    public long getFileSize(String fileName, String category) {
        try {
            Path filePath = getFilePath(fileName, category);
            return Files.size(filePath);
        } catch (IOException e) {
            log.error("Failed to get file size: {}/{}", category, fileName, e);
            return 0;
        }
    }

    public String getContentType(String fileName, String category) {
        try {
            Path filePath = getFilePath(fileName, category);
            return Files.probeContentType(filePath);
        } catch (IOException e) {
            log.error("Failed to get content type: {}/{}", category, fileName, e);
            return "application/octet-stream";
        }
    }
}