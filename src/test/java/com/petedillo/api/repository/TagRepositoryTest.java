package com.petedillo.api.repository;

import com.petedillo.api.model.Tag;
import com.petedillo.api.test.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("TagRepository Tests")
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    private Tag testTag;

    @BeforeEach
    void setUp() {
        testTag = TestDataFactory.tagBuilder()
                .name("java")
                .slug("java")
                .build();
    }

    @Test
    @DisplayName("should save and retrieve tag by name")
    void testSaveAndFindByName() {
        // Arrange
        Tag tag = TestDataFactory.tagBuilder()
                .name("spring-boot")
                .slug("spring-boot")
                .build();

        // Act
        Tag saved = tagRepository.save(tag);
        Optional<Tag> found = tagRepository.findByName("spring-boot");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getSlug()).isEqualTo("spring-boot");
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("should find tag by slug")
    void testFindBySlug() {
        // Arrange
        tagRepository.save(testTag);

        // Act
        Optional<Tag> found = tagRepository.findBySlug("java");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("java");
    }

    @Test
    @DisplayName("should enforce unique tag name constraint")
    void testUniqueNameConstraint() {
        // Arrange
        Tag tag1 = TestDataFactory.tagBuilder()
                .name("duplicate")
                .slug("duplicate")
                .build();
        Tag tag2 = TestDataFactory.tagBuilder()
                .name("duplicate")
                .slug("duplicate-2")
                .build();

        // Act & Assert
        tagRepository.save(tag1);
        assertThatThrownBy(() -> {
            tagRepository.save(tag2);
            tagRepository.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("should enforce unique slug constraint")
    void testUniqueSlugConstraint() {
        // Arrange
        Tag tag1 = TestDataFactory.tagBuilder()
                .name("tag1")
                .slug("duplicate-slug")
                .build();
        Tag tag2 = TestDataFactory.tagBuilder()
                .name("tag2")
                .slug("duplicate-slug")
                .build();

        // Act & Assert
        tagRepository.save(tag1);
        assertThatThrownBy(() -> {
            tagRepository.save(tag2);
            tagRepository.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("should find tags by name pattern (case-insensitive)")
    void testFindByNameContainingIgnoreCase() {
        // Arrange
        tagRepository.save(TestDataFactory.tagBuilder().name("Java").slug("java").build());
        tagRepository.save(TestDataFactory.tagBuilder().name("JavaScript").slug("javascript").build());
        tagRepository.save(TestDataFactory.tagBuilder().name("TypeScript").slug("typescript").build());

        // Act
        List<Tag> javaRelated = tagRepository.findByNameContainingIgnoreCase("java");

        // Assert
        assertThat(javaRelated).hasSize(2);
        assertThat(javaRelated).extracting("name").containsExactlyInAnyOrder("Java", "JavaScript");
    }

    @Test
    @DisplayName("should find all tags ordered by post count descending")
    void testFindAllOrderedByPostCountDesc() {
        // Arrange
        tagRepository.save(TestDataFactory.tagBuilder().name("popular").slug("popular").postCount(100).build());
        tagRepository.save(TestDataFactory.tagBuilder().name("medium").slug("medium").postCount(50).build());
        tagRepository.save(TestDataFactory.tagBuilder().name("rare").slug("rare").postCount(5).build());

        // Act
        List<Tag> tags = tagRepository.findAllByOrderByPostCountDesc();

        // Assert
        assertThat(tags).hasSize(3);
        assertThat(tags).extracting("postCount").containsExactly(100, 50, 5);
    }

    @Test
    @DisplayName("should update tag")
    void testUpdateTag() {
        // Arrange
        Tag saved = tagRepository.save(testTag);
        Long tagId = saved.getId();

        // Act
        saved.setPostCount(42);
        tagRepository.save(saved);

        // Assert
        Tag updated = tagRepository.findById(tagId).orElseThrow();
        assertThat(updated.getPostCount()).isEqualTo(42);
    }

    @Test
    @DisplayName("should delete tag")
    void testDeleteTag() {
        // Arrange
        Tag saved = tagRepository.save(testTag);
        Long tagId = saved.getId();

        // Act
        tagRepository.deleteById(tagId);

        // Assert
        Optional<Tag> deleted = tagRepository.findById(tagId);
        assertThat(deleted).isEmpty();
    }
}
