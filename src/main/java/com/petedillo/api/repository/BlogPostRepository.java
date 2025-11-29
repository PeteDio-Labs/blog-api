package com.petedillo.api.repository;

import com.petedillo.api.model.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    @Query("SELECT DISTINCT p FROM BlogPost p LEFT JOIN FETCH p.blogTags LEFT JOIN FETCH p.media m WHERE p.slug = :slug ORDER BY m.displayOrder ASC")
    Optional<BlogPost> findBySlug(@Param("slug") String slug);

    @Query("SELECT p FROM BlogPost p WHERE p.title ILIKE %:searchTerm% OR p.slug ILIKE %:searchTerm%")
    List<BlogPost> searchByTitleOrSlug(@Param("searchTerm") String searchTerm);
}
