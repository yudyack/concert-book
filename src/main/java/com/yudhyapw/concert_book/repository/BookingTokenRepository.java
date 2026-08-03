package com.yudhyapw.concert_book.repository;

import com.yudhyapw.concert_book.entity.BookingToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface BookingTokenRepository extends JpaRepository<BookingToken, UUID> {

    /**
     * Claims the token by flipping ISSUED to USED, but only when it is still ISSUED and belongs to the given user. 
     * Returns 1 when this request succeed and 0 if there was nothing to claim (already used, wrong owner, or unknown token)
     */
    @Modifying
    @Query("""
            UPDATE BookingToken t
            SET t.status = com.yudhyapw.concert_book.entity.TokenStatus.USED, t.updatedAt = :now
            WHERE t.id = :tokenId
              AND t.user.id = :userId
              AND t.status = com.yudhyapw.concert_book.entity.TokenStatus.ISSUED
            """)
    int claim(@Param("tokenId") UUID tokenId, @Param("userId") Long userId, @Param("now") OffsetDateTime now);
}