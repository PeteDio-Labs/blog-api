package com.petedillo.api.repository;

import com.petedillo.api.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Tag entities.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Find a tag by its name.
     *
     * @param name the tag name
     * @return an Optional containing the tag if found
     */
    Optional<Tag> findByName(String name);

    /**
     * Find a tag by its slug.
     *
     * @param slug the tag slug
     * @return an Optional containing the tag if found
     */
    Optional<Tag> findBySlug(String slug);

    /**
     * Find all tags containing the given text in their name (case-insensitive).
     *
     * @param namePattern the search pattern
     * @return a list of matching tags
     */
    List<Tag> findByNameContainingIgnoreCase(String namePattern);

    /**
     * Find all tags ordered by post count (most used first).
     *
     * @return a list of tags ordered by post count descending
     */
    List<Tag> findAllByOrderByPostCountDesc();
}
