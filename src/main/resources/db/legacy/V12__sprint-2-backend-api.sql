-- ============================================================================
-- Migration: V12__sprint-2-backend-api.sql
-- Description: Sprint 2 Backend/API Enhancements - Blog Post
-- Sprint: Sprint 2
-- Author: Pedro Delgadillo
-- Date: December 21, 2025
-- Purpose: Insert Sprint 2 Backend/API achievements blog post with comprehensive
--          details on 52 unit tests, admin UI, security layer, and API enhancements
-- ============================================================================

-- Insert Sprint 2 Backend/API Enhancement post
INSERT INTO blog_posts (
    title,
    slug,
    content,
    excerpt,
    status,
    is_featured,
    published_at
) VALUES (
    'Sprint 2 Part 1: Backend & API Enhancements',
    'sprint-2-backend-api-enhancements',
    '**Sprint Duration**: November 19-29, 2025
**Status**: ✅ 100% COMPLETE
**Focus**: API implementation, comprehensive testing, admin UI foundation, security hardening

---

## Overview

Sprint 2 Backend Phase focused on completing critical API functionality, implementing comprehensive unit test coverage (52 tests, 80%+ coverage), building a production-ready admin UI with Spring Security, and establishing security foundations for OAuth2 integration.

---

## Phase 1: API Critical Features ✅ COMPLETE
**Status**: ✅ COMPLETE (Nov 21, 2025)
**Completion**: 17/17 tasks

### Core API Enhancements

**GET /api/v1/posts/{slug} Endpoint**
- Fetch individual blog posts by URL-friendly slug
- Returns complete post with tags and media gallery
- Proper exception handling (ResourceNotFoundException)
- HTTP status codes: 200 (success), 404 (not found), 500 (server error)

**Environment Awareness Endpoint**
- `GET /api/v1/info` returns deployment environment metadata
- Includes: environment name, application version, build timestamp
- Enables UI to display environment banners (dev/stage/prod)
- Environment-specific configurations (dev vs. production profiles)

**Database Query Optimization**
- Named entity graphs for tag and media eager loading
- Reduced N+1 query problems through proper fetch strategies
- Custom repository query methods with `@Query` annotations
- Indexes on frequently queried columns (slug, status)

### Service Layer Enhancements

**BlogPostService Implementation**
```java
- getPostBySlug(String slug) → BlogPost
- getAllPublishedPosts() → List<BlogPost>
- createPost(BlogPostRequest) → BlogPost
- updatePost(Long id, BlogPostRequest) → BlogPost
- deletePost(Long id) → void
- publishPost(Long id) → BlogPost
```

**MediaService Implementation**
```java
- uploadMedia(MultipartFile) → BlogMedia
- deleteMedia(Long id) → void
- reorderMedia(Long postId, List<Long> mediaIds) → void
- getMediaByPost(Long postId) → List<BlogMedia>
```

**Features Delivered**:
- Service-layer exception handling
- Business logic validation
- Transactional operations for data consistency
- Lazy loading strategies for performance

### Admin Controllers - MVC + REST

**Admin MVC Controllers (Thymeleaf)**
- `AdminPostController` - Post management UI
- `AdminMediaController` - Media gallery UI
- `AdminLoginController` - Authentication flows
- Template-based forms with CSRF protection
- Spring Security integration for access control

**Admin REST Controllers (AJAX)**
- Media upload endpoints (multipart/form-data)
- Media reorder endpoints (JSON post body)
- Media delete endpoints (HTTP DELETE)
- Responsive error responses with proper HTTP codes

**Request/Response DTOs**
```java
BlogPostRequest - POST/PUT payloads
BlogPostResponse - API response format
BlogPostDTO - Lightweight DTO for lists
MediaDTO - Media item representation
CoverImageDTO - Cover image metadata
```

### Spring Security Implementation

**Authentication**
- Form-based login (/admin/login)
- BCrypt password hashing
- Session management with JSESSIONID
- "Remember me" functionality (optional)

**Authorization**
- Role-based access control (RBAC)
- POST/PUT/DELETE operations require ADMIN role
- GET operations accessible to ROLE_USER
- Method-level security with `@PreAuthorize`

**Security Features**
- CSRF protection on forms (`_csrf` hidden field)
- CORS configuration for API endpoints
- HTTPS redirect in production
- X-Frame-Options headers
- Content-Security-Policy headers

**OAuth2 Ready Architecture**
- Multiple authentication providers configured
- Extensible provider interface for Google/Apple login
- `AuthProvider` enum: LOCAL, GOOGLE, APPLE
- UserDetails implementation for custom user loading

### Admin UI Templates (Thymeleaf)

**Base Layout Template**
- Navigation bar with environment indicator
- Logout functionality
- Sidebar menu with section navigation
- Responsive grid layout (mobile-first)
- Neon cyberpunk theme styling

**Posts List Page**
- Table view of all blog posts
- Sort by title, date, status
- Filter by published/draft/archived
- Actions: Edit, Delete, View
- Pagination for large datasets

**Post Form Page**
- Title, slug, content (markdown) fields
- Cover image upload/selection
- Media gallery with drag-and-drop
- Tag input with autocomplete
- Featured flag toggle
- Publish/Save/Discard buttons

**Media Manager Page**
- Gallery view of uploaded media
- Drag-to-reorder functionality
- Bulk upload capability
- Delete with confirmation
- Search/filter by filename or type

### JavaScript Enhancements

**Markdown Editor with Live Preview**
- Real-time preview of markdown as user types
- Syntax highlighting for code blocks
- Toolbar buttons for common markdown patterns
- Character count and word count display
- Auto-save to browser localStorage

**Media Upload (Drag & Drop)**
- Drag files onto designated zone
- Click to select files from filesystem
- Multiple file upload in single request
- AJAX submission without page reload
- Progress bar for upload status
- Error handling with retry capability

**Media Reorder (Drag & Drop)**
- Reorder media items in gallery
- Visual feedback during drag
- Persist order via AJAX POST to backend
- Rollback on error with confirmation

**Admin Utilities**
- Shared utility functions (debounce, throttle)
- Form validation helpers
- CSRF token handling
- API error response parsing
- Toast notification system

### Docker Build Strategy

**Dockerfile.dev (Local Development)**
```dockerfile
FROM maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY . .
RUN mvn clean package
CMD ["java", "-jar", "target/api-0.6.1.jar"]
```
- Full Maven build inside container
- Incremental compilation with dependency caching
- Development dependencies included
- Fast iteration for local development

**Dockerfile (CI/CD Production)**
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/api-0.6.1.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```
- Uses pre-built JAR from Nexus
- Alpine Linux for minimal image size (~200MB vs 1.2GB)
- JRE only (not full JDK)
- Optimized for production deployment

### Unit Tests - 52 Tests Passing

**Test Coverage: 80%+**

**Repository Layer Tests (12 tests)**
```
✅ findBySlug() - Found and Not Found cases
✅ findAllPublished() - Ordering and filtering
✅ findByStatus() - Draft/Published/Archived filtering
✅ existsBySlug() - Slug uniqueness validation
✅ Custom query methods with sorting
```

**Service Layer Tests (20 tests)**
```
✅ getPostBySlug() - Success and exception cases
✅ createPost() - Validation and persistence
✅ updatePost() - Partial updates and conflicts
✅ publishPost() - Status transitions
✅ deletePost() - Cascade deletion of media
✅ getMediaByPost() - Eager loading verification
✅ uploadMedia() - File validation and storage
✅ reorderMedia() - Order persistence
```

**Controller Layer Tests (20 tests)**
```
✅ GET /api/v1/posts/{slug} - Success and 404
✅ GET /api/v1/posts - Pagination and filtering
✅ POST /admin/posts - Creation with validation
✅ PUT /admin/posts/{id} - Updates and conflicts
✅ DELETE /admin/posts/{id} - Cascade operations
✅ POST /admin/media/upload - Multipart handling
✅ GET /api/v1/info - Environment metadata
✅ Exception handler responses
```

**Test Infrastructure**
- `@DataJpaTest` for repository isolation
- `@WebMvcTest` for controller testing
- `@SpringBootTest` for integration tests
- H2 in-memory database for test isolation
- Mockito for service mocking
- TestRestTemplate for API testing
- `application-test.properties` for test configuration

**Test Profiles**
```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
logging.level.root=WARN
```

---

## Tags Implementation

**BlogTag Entity (One-to-Many)**
```java
@Entity
@Table(name = "blog_tags")
public class BlogTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "blog_post_id")
    private BlogPost blogPost;

    private String tagName;
}
```

**Normalized Tag Entity (Migration V11)**
```java
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(unique = true)
    private String slug;

    private Integer postCount;
}
```

**BlogPost-Tag Relationship**
```java
@ManyToMany
@JoinTable(
    name = "post_tags",
    joinColumns = @JoinColumn(name = "post_id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id")
)
private Set<Tag> tags = new HashSet<>();
```

**Features**
- Tag autocomplete in admin UI
- Full-text search on tag names
- Tag cloud generation for frontend
- Post count per tag (cached)
- Tag slug for URL-friendly links

---

## Configuration & Environment Profiles

**Application Profiles**
- `application.properties` - Base configuration
- `application-dev.properties` - Local development
- `application-prod.properties` - Production deployment
- `application-stage.properties` - Staging environment
- `application-test.properties` - Test environment

**Database Configuration**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/blog
spring.datasource.username=$${DB_USERNAME}
spring.datasource.password=$${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**Security Configuration**
```properties
spring.security.user.name=admin
spring.security.user.password=$${ADMIN_PASSWORD}
server.ssl.enabled=true
server.ssl.key-store=$${SSL_KEYSTORE_PATH}
```

---

## Success Metrics

| Metric | Target | Achieved |
|--------|--------|----------|
| Unit Test Coverage | 80%+ | ✅ 80%+ (52 tests) |
| API Endpoints Functional | All core endpoints | ✅ 7/7 |
| Admin UI Operational | All CRUD operations | ✅ Complete |
| Security Implementation | Spring Security integrated | ✅ Implemented |
| Docker Build Strategy | Dev + CI/CD dual approach | ✅ Dual Dockerfile |
| Environment Awareness | API includes metadata | ✅ /api/v1/info |
| Service Layer Complete | Business logic extracted | ✅ BlogPostService, MediaService |
| Exception Handling | GlobalExceptionHandler | ✅ Comprehensive |

---

## Key Challenges & Solutions

### Challenge 1: N+1 Query Problem
**Issue**: Each blog post loaded tags separately
**Solution**: Implemented Named Entity Graphs with eager loading
```java
@NamedEntityGraph(name = "BlogPost.tags",
    attributeNodes = @NamedAttributeNode("tags"))
```

### Challenge 2: CSRF Protection in AJAX
**Issue**: CSRF tokens not included in AJAX requests
**Solution**: Auto-include CSRF token in request headers via JavaScript

### Challenge 3: Multipart File Upload Security
**Issue**: File upload validation missing
**Solution**: Added file type, size, and virus scan validation

### Challenge 4: Password Hashing
**Issue**: Plaintext passwords stored in tests
**Solution**: Used BCryptPasswordEncoder for all passwords

### Challenge 5: Test Database Isolation
**Issue**: Tests affecting each other with shared state
**Solution**: H2 in-memory database with `@Transactional` rollback

---

## Lessons Learned

1. **Named Entity Graphs** are superior to Hibernate.LAZY for performance optimization
2. **Test-driven development** catches security issues early (CSRF, SQL injection)
3. **Dual Dockerfile strategy** balances dev experience with production efficiency
4. **Service layer abstraction** enables easier testing and business logic reuse
5. **Security-first architecture** (BCrypt, CSRF, validation) prevents technical debt
6. **Environment profiles** enable seamless transitions between dev/stage/prod
7. **Comprehensive documentation** in code (JavaDoc, inline comments) saves debugging time
8. **Mockito for testing** allows unit tests to be truly isolated from database
9. **GlobalExceptionHandler** provides consistent error responses across all endpoints
10. **Early OAuth2 design** enables painless future multi-provider authentication

---

## Technical Stack - Backend

- **Framework**: Spring Boot 3.5.7
- **Java Version**: Java 21
- **Database**: PostgreSQL 15
- **ORM**: Hibernate (JPA)
- **Security**: Spring Security 6 + JWT (JJWT)
- **Testing**: JUnit 5, Mockito, TestRestTemplate
- **Build**: Maven 3.9
- **Containerization**: Docker (Alpine + JRE-only)
- **Authentication**: Form-based + OAuth2-ready

---

## GitHub Repositories

```
API:         https://github.com/petedillo/blog-api
Tag:         v0.6.1
Branch:      main
Documentation: 17 implementation guides
```

---

## Conclusion

Sprint 2 Backend Phase delivered a production-ready API with comprehensive test coverage, security-first architecture, and a fully functional admin UI. The 52 unit tests passing at 80%+ coverage provide confidence in code quality, while the dual Docker strategy enables both efficient local development and lean production deployments.

**Key Achievement**: From zero tests to 80%+ coverage with 52 tests, all critical API paths validated and secured.

---

**Completion Date**: November 21, 2025
**Status**: ✅ COMPLETE
**Documentation**: 17 files, 85KB
**Tests Passing**: 52 (BUILD SUCCESS)',
    'Building production-ready backend: 52 unit tests (80%+ coverage), Spring Security integration, admin UI with Thymeleaf, comprehensive service layer.',
    'PUBLISHED',
    FALSE,
    CURRENT_TIMESTAMP
);

-- Get the inserted post ID and add tags
DO $$
DECLARE
    post_id BIGINT;
BEGIN
    SELECT id INTO post_id FROM blog_posts WHERE slug = 'sprint-2-backend-api-enhancements';
    
    -- Insert tags into tags table if they don't exist, then link to post
    INSERT INTO tags (name, slug) VALUES 
        ('sprint-2', 'sprint-2'),
        ('backend', 'backend'),
        ('api', 'api'),
        ('testing', 'testing'),
        ('spring-boot', 'spring-boot')
    ON CONFLICT (name) DO NOTHING;
    
    -- Link tags to post via post_tags junction table
    INSERT INTO post_tags (post_id, tag_id)
    SELECT post_id, t.id FROM tags t WHERE name IN ('sprint-2', 'backend', 'api', 'testing', 'spring-boot')
    ON CONFLICT DO NOTHING;
    
    -- Update tag post counts
    UPDATE tags SET post_count = (SELECT COUNT(*) FROM post_tags WHERE tag_id = tags.id)
    WHERE name IN ('sprint-2', 'backend', 'api', 'testing', 'spring-boot');
END $$
;

-- ============================================================================
-- Solo Developer: Pedro Delgadillo
-- Status: Sprint 2 Backend Phase Complete
-- Next: V13__sprint-2-frontend-ui.sql
