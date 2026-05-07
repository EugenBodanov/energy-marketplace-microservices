package com.energy.marketplace.listing.adapter.out.persistence;

import com.energy.marketplace.listing.domain.model.Listing;
import com.energy.marketplace.listing.domain.valueObject.Capacity;
import com.energy.marketplace.listing.domain.valueObject.ListingPrice;
import com.energy.marketplace.listing.domain.valueObject.ListingStatus;

public class ListingPersistenceMapper {

    public ListingJpaEntity toEntity(Listing listing) {
        return new ListingJpaEntity(
                listing.getId(),
                listing.getSellerId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice().getAmount(),
                listing.getPrice().getCurrency(),
                listing.getCapacity().getValue(),
                listing.getCapacity().getUnit(),
                listing.getStatus(),
                listing.getReservationReference(),
                listing.getCreatedAt(),
                listing.getUpdatedAt()
        );
    }

    public Listing toDomain(ListingJpaEntity entity) {
        return new Listing(
                entity.getId(),
                entity.getSellerId(),
                entity.getTitle(),
                entity.getDescription(),
                new ListingPrice(entity.getPriceAmount(), entity.getPriceCurrency()),
                new Capacity(entity.getCapacityValue(), entity.getCapacityUnit()),
                entity.getStatus(),
                entity.getReservationReference(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

