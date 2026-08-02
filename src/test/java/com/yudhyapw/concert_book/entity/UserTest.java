package com.yudhyapw.concert_book.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void constructorSetsName() {
        User user = new User("Yudhya");

        assertEquals("Yudhya", user.getName());
        assertNull(user.getId(), "id must stay null until the database assigns it");
    }
}