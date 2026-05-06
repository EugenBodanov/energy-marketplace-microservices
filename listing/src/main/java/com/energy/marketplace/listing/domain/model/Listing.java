package com.energy.marketplace.listing.domain.model;

import com.energy.marketplace.listing.domain.exception.ListingInvalidStateException;
import com.energy.marketplace.listing.domain.valueObject.Capacity;
import com.energy.marketplace.listing.domain.valueObject.ListingPrice;
import com.energy.marketplace.listing.domain.valueObject.ListingStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Listing {
    private Long id;
    private Long sellerId;
    private String title;
    private String description;
    private ListingPrice price;
    private Capacity capacity;
    private ListingStatus status;
    private Long reservationReference;
    private Instant createdAt;
    private Instant updatedAt;

    // Setters for persistence mapping
    public void setId(Long id) { this.id = id; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(ListingPrice price) { this.price = price; }
    public void setCapacity(Capacity capacity) { this.capacity = capacity; }
    public void setStatus(ListingStatus status) { this.status = status; }
    public void setReservationReference(Long reservationReference) { this.reservationReference = reservationReference; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static Listing create(
            Long sellerId,
            String title,
            String description,
            ListingPrice price,
            Capacity capacity
    ) {
        Listing listing = new Listing();
        listing.sellerId = sellerId;
        listing.title = title;
        listing.description = description;
        listing.price = price;
        listing.capacity = capacity;
        listing.status = ListingStatus.AVAILABLE;
        listing.createdAt = Instant.now();
        listing.updatedAt = Instant.now();
        return listing;
    }

    public void reserve(Long tradeId) {
        if (!status.equals(ListingStatus.AVAILABLE)) {
            throw new ListingInvalidStateException(
                    "Listing must be AVAILABLE to be reserved, but current status is: " + status
            );
        }
        this.status = ListingStatus.RESERVED;
        this.reservationReference = tradeId;
        this.updatedAt = Instant.now();
    }

    public void release() {
        if (!status.equals(ListingStatus.RESERVED)) {
            throw new ListingInvalidStateException(
                    "Listing must be RESERVED to be released, but current status is: " + status
            );
        }
        this.status = ListingStatus.AVAILABLE;
        this.reservationReference = null;
        this.updatedAt = Instant.now();
    }

    public void close() {
        if (!status.equals(ListingStatus.RESERVED)) {
            throw new ListingInvalidStateException(
                    "Listing must be RESERVED to be closed, but current status is: " + status
            );
        }
        this.status = ListingStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void update(String title, String description, ListingPrice price, Capacity capacity) {
        if (!status.equals(ListingStatus.AVAILABLE)) {
            throw new ListingInvalidStateException(
                    "Can only update AVAILABLE listings, but current status is: " + status
            );
        }
        this.title = title;
        this.description = description;
        this.price = price;
        this.capacity = capacity;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = ListingStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }
}

