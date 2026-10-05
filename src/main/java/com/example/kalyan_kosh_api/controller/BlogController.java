package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.entity.BlogPost;
import com.example.kalyan_kosh_api.service.BlogPostService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BlogController {

    private final BlogPostService blogPostService;

    public BlogController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    @GetMapping("/public/blogs")
    public ResponseEntity<List<BlogPost>> getPublicBlogs() {
        return ResponseEntity.ok(blogPostService.getPublicBlogs());
    }

    @GetMapping("/public/blogs/{id}")
    public ResponseEntity<BlogPost> getPublicBlog(@PathVariable Long id) {
        return ResponseEntity.ok(blogPostService.getPublicBlog(id));
    }

    @GetMapping("/admin/blogs")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<List<BlogPost>> getAdminBlogs() {
        return ResponseEntity.ok(blogPostService.getAdminBlogs());
    }

    @PostMapping(value = "/admin/blogs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<BlogPost> createBlog(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "isActive", defaultValue = "true") boolean active
    ) {
        return ResponseEntity.ok(blogPostService.createBlog(title, content, image, active));
    }

    @PutMapping(value = "/admin/blogs/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<BlogPost> updateBlog(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "isActive", defaultValue = "true") boolean active
    ) {
        return ResponseEntity.ok(blogPostService.updateBlog(id, title, content, image, active));
    }

    @DeleteMapping("/admin/blogs/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteBlog(@PathVariable Long id) {
        blogPostService.deleteBlog(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Blog post deleted successfully"
        ));
    }
}
