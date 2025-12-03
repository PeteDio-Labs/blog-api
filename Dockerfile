# =============================================================================
# Production Dockerfile for Blog API (CI/CD)
# =============================================================================
# This Dockerfile expects a pre-built JAR file in the target/ directory.
# Used by GitHub Actions CI/CD pipeline which builds the JAR separately.
#
# For local development with docker-compose, use Dockerfile.dev which
# includes the Maven build step.
#
# CI/CD Build Process:
#   1. GitHub Actions runs: mvn clean package
#   2. JAR is deployed to Nexus Maven repository
#   3. JAR is copied to build context: cp target/*.jar .
#   4. Docker builds this image with the pre-built JAR
#   5. Image is pushed to docker.toastedbytes.com/blog-api
# =============================================================================

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the pre-built JAR from the build context root
# (GitHub Actions workflow copies it: cp target/*.jar .)
COPY api-*.jar app.jar

# Optional: Add build metadata
ARG BUILD_DATE
LABEL build_date="${BUILD_DATE}"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
