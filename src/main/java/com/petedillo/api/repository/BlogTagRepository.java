package com.petedillo.api.repository;

import com.petedillo.api.model.BlogTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogTagRepository extends JpaRepository<BlogTag, Long> {

    /**
     * Find tag by name (case-insensitive)
     */
    Optional<BlogTag> findByTagNameIgnoreCase(String tagName);

    /**
     * Find all tags ordered alphabetically by tag name
     */
    List<BlogTag> findAllByOrderByTagNameAsc();
}
