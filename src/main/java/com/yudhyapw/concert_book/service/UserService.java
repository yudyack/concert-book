package com.yudhyapw.concert_book.service;

import com.yudhyapw.concert_book.dto.CreateUserRequest;
import com.yudhyapw.concert_book.dto.UserResponse;
import com.yudhyapw.concert_book.entity.User;
import com.yudhyapw.concert_book.exception.BookingConflictException;
import com.yudhyapw.concert_book.exception.ResourceNotFoundException;
import com.yudhyapw.concert_book.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("user " + userId + " not found"));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByName(request.name())) {
            throw new BookingConflictException("user name '" + request.name() + "' is already taken");
        }
        return UserResponse.from(userRepository.save(new User(request.name())));
    }
}