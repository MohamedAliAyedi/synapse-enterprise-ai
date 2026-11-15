# Feign Client Guide for synapse-enterprise-ai

## Overview

The **synapse-enterprise-ai** platform consists of several Spring Boot microservices that communicate with each other via HTTP. To simplify inter‑service calls we use **OpenFeign**. This document provides a step‑by‑step guide on:

1. Adding the Feign dependency.
2. Enabling Feign clients in each service.
3. Defining client interfaces.
4. Configuring time‑outs, logging, and fallback mechanisms.
5. Writing integration tests with **Spring Cloud Contract** and **Testcontainers**.

## 1. Add Feign Dependency

In each service's `pom.xml` add:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

Make sure the Spring Cloud BOM is imported in the parent `pom.xml`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 2. Enable Feign Clients

Add `@EnableFeignClients` to the main application class of the service that will *consume* other services. Example for `ai-orchestrator-service`:

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.aiorchestrator.client")
public class AiOrchestratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiOrchestratorApplication.class, args);
    }
}
```

## 3. Define a Feign Client Interface

Create an interface in a dedicated `client` package. Use Spring MVC annotations to map the remote endpoints.

```java
package com.example.aiorchestrator.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for the Document Indexer Service.
 */
@FeignClient(name = "document-indexer-service", url = "${document.indexer.url}")
public interface DocumentIndexerClient {

    @GetMapping("/api/v1/documents/{id}/chunks")
    ChunkResponse getDocumentChunks(@PathVariable("id") String documentId);
}
```

- `name` matches the service registration name in Eureka.
- `url` can be overridden via property (useful for local development).

### 3.1. Configuration (Optional)

If you need custom time‑outs or logging, define a configuration class:

```java
package com.example.aiorchestrator.client;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentIndexerFeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(5000, 30000); // connect timeout, read timeout
    }
}
```

Reference it in `@FeignClient` with `configuration = DocumentIndexerFeignConfig.class`.

## 4. Fallback (Circuit Breaker) Support

Feign integrates with Resilience4j or Spring Cloud Circuit Breaker. Define a fallback implementation:

```java
package com.example.aiorchestrator.client;

import org.springframework.stereotype.Component;

@Component
public class DocumentIndexerClientFallback implements DocumentIndexerClient {
    @Override
    public ChunkResponse getDocumentChunks(String documentId) {
        // Return an empty response or cached data
        return new ChunkResponse();
    }
}
```

Add `fallback = DocumentIndexerClientFallback.class` to the `@FeignClient` annotation.

## 5. Integration Testing

### 5.1. Testcontainers + WireMock

Use Testcontainers to spin up a WireMock container that mimics the remote service.

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class DocumentIndexerClientIT {

    @Container
    static WireMockContainer wireMock = new WireMockContainer("wiremock/wiremock:2.35.0")
            .withExposedPorts(8080);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("document.indexer.url", () -> "http://" + wireMock.getHost() + ":" + wireMock.getFirstMappedPort());
    }

    @Autowired
    private DocumentIndexerClient client;

    @Test
    void shouldReturnChunks() {
        wireMock.stubFor(get(urlPathMatching("/api/v1/documents/.*/chunks"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"chunks\":[]}")));

        ChunkResponse response = client.getDocumentChunks("123");
        assertNotNull(response);
        assertTrue(response.getChunks().isEmpty());
    }
}
```

### 5.2. Spring Cloud Contract (Optional)

Generate stubs from contract definitions and verify that the Feign client adheres to the contract.

## 6. Property Configuration

Add the following defaults to `application.yml` of the consuming service:

```yaml
document:
  indexer:
    url: http://document-indexer-service:8080
```

Developers can override the URL via environment variable `DOCUMENT_INDEXER_URL`.

---

**Next steps**: Implement the Feign client interfaces in the remaining services (`notification-service`, `document-indexer-service` if they act as consumers) and add integration tests as described.
