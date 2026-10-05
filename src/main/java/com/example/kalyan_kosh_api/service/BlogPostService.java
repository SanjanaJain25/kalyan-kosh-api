package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.BlogPost;
import com.example.kalyan_kosh_api.repository.BlogPostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class BlogPostService {

    private static final long MAX_IMAGE_SIZE_BYTES = 3L * 1024L * 1024L;

    private final BlogPostRepository blogPostRepository;

    public BlogPostService(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    public List<BlogPost> getPublicBlogs() {
        return blogPostRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    public BlogPost getPublicBlog(Long id) {
        return blogPostRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Blog post not found"));
    }

    public List<BlogPost> getAdminBlogs() {
        return blogPostRepository.findAllByOrderByCreatedAtDesc();
    }

    public BlogPost createBlog(
            String title,
            String content,
            MultipartFile image,
            boolean active
    ) {
        validateText(title, content);

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Blog image is required");
        }

        BlogPost blogPost = new BlogPost();
        blogPost.setTitle(title.trim());
        blogPost.setContent(content.trim());
        blogPost.setActive(active);
        applyImage(blogPost, image);

        return blogPostRepository.save(blogPost);
    }

    public BlogPost updateBlog(
            Long id,
            String title,
            String content,
            MultipartFile image,
            boolean active
    ) {
        validateText(title, content);

        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Blog post not found"));

        blogPost.setTitle(title.trim());
        blogPost.setContent(content.trim());
        blogPost.setActive(active);

        if (image != null && !image.isEmpty()) {
            applyImage(blogPost, image);
        }

        return blogPostRepository.save(blogPost);
    }

    public void deleteBlog(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Blog post not found"));
        blogPostRepository.delete(blogPost);
    }

    private void validateText(String title, String content) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Blog title is required");
        }
        if (title.trim().length() > 255) {
            throw new IllegalArgumentException("Blog title must be 255 characters or less");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Blog content is required");
        }
    }

    private void applyImage(BlogPost blogPost, MultipartFile image) {
        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("Blog image must be 3 MB or smaller");
        }

        String contentType = image.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new IllegalArgumentException("Only JPG, PNG and WEBP images are allowed");
        }

        try {
            String base64 = Base64.getEncoder().encodeToString(image.getBytes());
            blogPost.setImageBase64(base64);
            blogPost.setImageContentType(contentType);
            blogPost.setImageFileName(sanitizeFileName(image.getOriginalFilename()));
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read blog image", ex);
        }
    }

    private boolean isAllowedImageType(String contentType) {
        return "image/jpeg".equalsIgnoreCase(contentType)
                || "image/png".equalsIgnoreCase(contentType)
                || "image/webp".equalsIgnoreCase(contentType);
    }

    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return null;
        }
        String fileName = originalFileName.replace("\\", "/");
        int lastSlash = fileName.lastIndexOf('/');
        if (lastSlash >= 0) {
            fileName = fileName.substring(lastSlash + 1);
        }
        return fileName.length() > 255 ? fileName.substring(fileName.length() - 255) : fileName;
    }
}
