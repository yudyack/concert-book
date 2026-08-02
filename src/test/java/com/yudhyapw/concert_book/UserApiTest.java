package com.yudhyapw.concert_book;

import com.yudhyapw.concert_book.dto.CreateUserRequest;
import com.yudhyapw.concert_book.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private RestTestClient client;

    @Test
    void createThenFetchUser() {
        UserResponse created = client.post().uri("/api/users")
                .body(new CreateUserRequest("Alice"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponse.class)
                .returnResult().getResponseBody();

        assertNotNull(created);
        assertNotNull(created.userId());

        client.get().uri("/api/users/" + created.userId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(user -> assertEquals("Alice", user.name()));
    }

    @Test
    void unknownUserReturns404() {
        client.get().uri("/api/users/999999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void blankNameReturns400() {
        client.post().uri("/api/users")
                .body(new CreateUserRequest("  "))
                .exchange()
                .expectStatus().isBadRequest();
    }
}