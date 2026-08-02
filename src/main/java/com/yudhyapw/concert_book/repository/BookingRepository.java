package com.yudhyapw.concert_book.repository;

import com.yudhyapw.concert_book.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByTokenId(UUID tokenId);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
}