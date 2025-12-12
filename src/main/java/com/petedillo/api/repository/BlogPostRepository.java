package com.petedillo.api.repository;

import com.petedillo.api.model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    // Fix Cartesian product: Load post with tags using EntityGraph
    @EntityGraph(value = "BlogPost.tags", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM BlogPost p WHERE p.slug = :slug")
    Optional<BlogPost> findBySlugWithTags(@Param("slug") String slug);

    // Load media separately with proper ordering
    @Query("SELECT p FROM BlogPost p LEFT JOIN FETCH p.media m WHERE p.slug = :slug ORDER BY m.displayOrder ASC")
    Optional<BlogPost> findBySlugWithMedia(@Param("slug") String slug);

    // Deprecated: Use findBySlugWithTags and findBySlugWithMedia instead
    // Keeping for backward compatibility during migration
    @Deprecated
    @Query("SELECT DISTINCT p FROM BlogPost p LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.media m WHERE p.slug = :slug ORDER BY m.displayOrder ASC")
    Optional<BlogPost> findBySlug(@Param("slug") String slug);

    // Fix N+1: Load tags with EntityGraph
    @EntityGraph(value = "BlogPost.tags", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM BlogPost p WHERE p.title ILIKE %:searchTerm% OR p.slug ILIKE %:searchTerm%")
    List<BlogPost> searchByTitleOrSlug(@Param("searchTerm") String searchTerm);

    /**
     * Find posts by tag name (case-insensitive)
     * Tags are loaded via the join, so use EntityGraph to ensure they're loaded
     */
    @EntityGraph(value = "BlogPost.tags", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT DISTINCT p FROM BlogPost p JOIN p.tags t WHERE LOWER(t.name) = LOWER(:tagName)")
    List<BlogPost> findByTags_NameIgnoreCase(@Param("tagName") String tagName);

    /**
     * Find all posts ordered by published date descending
     * Fix N+1: Use EntityGraph to load tags
     */
    @EntityGraph(value = "BlogPost.tags", type = EntityGraph.EntityGraphType.LOAD)
    List<BlogPost> findAllByOrderByPublishedAtDesc();

    /**
     * Override findAll to load tags efficiently
     */
    @Override
    @EntityGraph(value = "BlogPost.tags", type = EntityGraph.EntityGraphType.LOAD)
    List<BlogPost> findAll();

    /**
     * Override findById to load tags efficiently
     */
    @Override
    @EntityGraph(value = "BlogPost.tags", type = EntityGraph.EntityGraphType.LOAD)
    Optional<BlogPost> findById(Long id);

    /**
     * Find posts by status with pagination
     */
    Page<BlogPost> findByStatus(String status, Pageable pageable);

    /**
     * Find posts by title containing (case-insensitive) with pagination
     */
    Page<BlogPost> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Count posts by status
     */
    Long countByStatus(String status);

    /**
     * Search posts across title, content, and tags by status
     * Searches are case-insensitive
     */
    @EntityGraph(value = "BlogPost.tags", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT DISTINCT p FROM BlogPost p LEFT JOIN p.tags t WHERE " +
           "p.status = :status AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<BlogPost> searchByStatusAndQuery(@Param("searchTerm") String searchTerm,
                                           @Param("status") String status,
                                           Pageable pageable);
}
