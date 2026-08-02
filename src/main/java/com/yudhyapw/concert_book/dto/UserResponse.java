package com.yudhyapw.concert_book.dto;

import com.yudhyapw.concert_book.entity.User;

public record UserResponse(Long userId, String name) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName());
    }
}