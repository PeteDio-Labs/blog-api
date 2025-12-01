package com.petedillo.api.repository;

import com.petedillo.api.model.BlogMedia;
import com.petedillo.api.model.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogMediaRepository extends JpaRepository<BlogMedia, Long> {

    /**
     * Find all media for a specific blog post, ordered by display order
     */
    List<BlogMedia> findByBlogPostIdOrderByDisplayOrderAsc(Long postId);

    /**
     * Find all media across all posts ordered by created date descending (for media manager)
     */
    List<BlogMedia> findAllByOrderByCreatedAtDesc();

    /**
     * Count media items for a specific post
     */
    long countByBlogPost(BlogPost blogPost);

    /**
     * Delete all media for a specific post
     */
    void deleteByBlogPost(BlogPost blogPost);
}
