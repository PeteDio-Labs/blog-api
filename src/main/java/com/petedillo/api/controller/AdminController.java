package com.petedillo.api.controller;

import com.petedillo.api.dto.BlogPostDTO;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.service.BlogPostService;
import com.petedillo.api.service.MediaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manage")
public class AdminController {

    private final BlogPostService blogPostService;
    private final MediaService mediaService;

    public AdminController(BlogPostService blogPostService, MediaService mediaService) {
        this.blogPostService = blogPostService;
        this.mediaService = mediaService;
    }

    // === Login Page ===
    
    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("error", true);
        }
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        return "admin/login";
    }

    // === Posts List Page ===
    
    @GetMapping("/posts")
    public String listPosts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag,
            Model model
    ) {
        List<BlogPost> posts;
        
        if (tag != null && !tag.isEmpty()) {
            posts = blogPostService.getPostsByTag(tag);
        } else if (search != null && !search.isEmpty()) {
            // Simple search implementation - filter by title/content containing search term
            posts = blogPostService.getAllPostsSorted().stream()
                    .filter(p -> p.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                                 (p.getContent() != null && p.getContent().toLowerCase().contains(search.toLowerCase())))
                    .collect(Collectors.toList());
        } else {
            posts = blogPostService.getAllPostsSorted();
        }
        
        model.addAttribute("posts", posts);
        model.addAttribute("allTags", blogPostService.getAllTags());
        model.addAttribute("search", search);
        model.addAttribute("tag", tag);
        
        return "admin/posts";
    }

    // === New Post Form ===
    
    @GetMapping("/posts/new")
    public String newPostForm(Model model) {
        model.addAttribute("post", new BlogPost());
        model.addAttribute("allTags", blogPostService.getAllTags());
        return "admin/post-form";
    }

    // === Edit Post Form ===
    
    @GetMapping("/posts/{id}/edit")
    public String editPostForm(@PathVariable long id, RedirectAttributes redirectAttributes, Model model) {
        BlogPost post = blogPostService.getPostById(id);
        if (post == null) {
            redirectAttributes.addFlashAttribute("error", "Post not found");
            return "redirect:/manage/posts";
        }
        
        model.addAttribute("post", post);
        model.addAttribute("allTags", blogPostService.getAllTags());
        return "admin/post-form";
    }

    // === Create Post ===
    
    @PostMapping("/posts")
    public String createPost(
            @ModelAttribute BlogPostDTO postDTO,
            RedirectAttributes redirectAttributes
    ) {
        try {
            BlogPost createdPost = blogPostService.createPost(
                    postDTO.getTitle(),
                    postDTO.getContent(),
                    postDTO.getExcerpt(),
                    postDTO.getStatus(),
                    postDTO.getTagsAsSet()
            );
            redirectAttributes.addFlashAttribute("success", "Post created successfully");
            return "redirect:/manage/posts/" + createdPost.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create post: " + e.getMessage());
            return "redirect:/manage/posts/new";
        }
    }

    // === Update Post ===
    
    @PostMapping("/posts/{id}")
    public String updatePost(
            @PathVariable long id,
            @ModelAttribute BlogPostDTO postDTO,
            RedirectAttributes redirectAttributes
    ) {
        try {
            blogPostService.updatePost(
                    id,
                    postDTO.getTitle(),
                    postDTO.getContent(),
                    postDTO.getExcerpt(),
                    postDTO.getStatus(),
                    postDTO.getTagsAsSet()
            );
            redirectAttributes.addFlashAttribute("success", "Post updated successfully");
            return "redirect:/manage/posts/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update post: " + e.getMessage());
            return "redirect:/manage/posts/" + id + "/edit";
        }
    }

    // === Delete Post ===
    
    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            blogPostService.deletePost(id);
            redirectAttributes.addFlashAttribute("success", "Post deleted successfully");
            return "redirect:/manage/posts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete post: " + e.getMessage());
            return "redirect:/manage/posts";
        }
    }

    // === Media Manager ===
    
    @GetMapping("/media")
    public String mediaManager(Model model) {
        model.addAttribute("allMedia", mediaService.getAllMedia());
        return "admin/media";
    }
}
