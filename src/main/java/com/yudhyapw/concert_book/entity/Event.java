package com.yudhyapw.concert_book.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ms_event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "event_name", nullable = false)
    private String name;

    @Column(name = "event_venue")
    private String venue;

    @Column(name = "event_time")
    private OffsetDateTime eventTime;

    @Column(name = "sale_start", nullable = false)
    private OffsetDateTime saleStart;

    @Column(name = "sale_end", nullable = false)
    private OffsetDateTime saleEnd;

    @Column(name = "ticket_total", nullable = false)
    private int ticketTotal;

    @Column(name = "ticket_available", nullable = false)
    private int ticketAvailable;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Event() {
    }

    public Event(String name, String venue, OffsetDateTime eventTime,
                 OffsetDateTime saleStart, OffsetDateTime saleEnd, int ticketTotal) {
        this.name = name;
        this.venue = venue;
        this.eventTime = eventTime;
        this.saleStart = saleStart;
        this.saleEnd = saleEnd;
        this.ticketTotal = ticketTotal;
        this.ticketAvailable = ticketTotal;
    }

    public boolean isOnSaleAt(OffsetDateTime moment) {
        return !moment.isBefore(saleStart) && !moment.isAfter(saleEnd);
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVenue() {
        return venue;
    }

    public OffsetDateTime getEventTime() {
        return eventTime;
    }

    public OffsetDateTime getSaleStart() {
        return saleStart;
    }

    public OffsetDateTime getSaleEnd() {
        return saleEnd;
    }

    public int getTicketTotal() {
        return ticketTotal;
    }

    public int getTicketAvailable() {
        return ticketAvailable;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public void setEventTime(OffsetDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public void setSaleStart(OffsetDateTime saleStart) {
        this.saleStart = saleStart;
    }

    public void setSaleEnd(OffsetDateTime saleEnd) {
        this.saleEnd = saleEnd;
    }

    public void setTicketTotal(int ticketTotal) {
        this.ticketTotal = ticketTotal;
    }

    public void setTicketAvailable(int ticketAvailable) {
        this.ticketAvailable = ticketAvailable;
    }

}