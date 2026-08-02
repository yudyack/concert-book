package com.yudhyapw.concert_book.repository;

import com.yudhyapw.concert_book.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByNameContainingIgnoreCase(String name);

    /**
     * Decrements available tickets, but only when enough.
     * Returns 1 when the decrement succeeded and 0 when stock was insufficient,
     */
    @Modifying
    @Query("""
            UPDATE Event e
            SET e.ticketAvailable = e.ticketAvailable - :quantity
            WHERE e.id = :eventId AND e.ticketAvailable >= :quantity
            """)
    int decrementAvailableTickets(@Param("eventId") Long eventId, @Param("quantity") int quantity);
}