package com.yudhyapw.concert_book.repository;

import com.yudhyapw.concert_book.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByName(String name);
}