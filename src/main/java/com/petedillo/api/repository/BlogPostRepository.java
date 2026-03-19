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

    @EntityGraph(value = "BlogPost.tags", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM BlogPost p WHERE p.slug = :slug")
    Optional<BlogPost> findBySlugWithTags(@Param("slug") String slug);

    @EntityGraph(value = "BlogPost.tags", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM BlogPost p WHERE p.title ILIKE %:searchTerm% OR p.slug ILIKE %:searchTerm%")
    List<BlogPost> searchByTitleOrSlug(@Param("searchTerm") String searchTerm);

    @EntityGraph(value = "BlogPost.tags", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT DISTINCT p FROM BlogPost p JOIN p.tags t WHERE LOWER(t.name) = LOWER(:tagName)")
    List<BlogPost> findByTags_NameIgnoreCase(@Param("tagName") String tagName);

    @EntityGraph(value = "BlogPost.tags", type = EntityGraph.EntityGraphType.LOAD)
    List<BlogPost> findAllByOrderByPublishedAtDesc();

    @Override
    @EntityGraph(value = "BlogPost.tags", type = EntityGraph.EntityGraphType.LOAD)
    List<BlogPost> findAll();

    @Override
    @EntityGraph(value = "BlogPost.tags", type = EntityGraph.EntityGraphType.LOAD)
    Optional<BlogPost> findById(Long id);

    Page<BlogPost> findByStatus(String status, Pageable pageable);

    Page<BlogPost> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Long countByStatus(String status);

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
