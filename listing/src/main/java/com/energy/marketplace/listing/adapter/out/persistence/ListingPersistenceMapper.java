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
        Listing listing = new Listing();
        listing.setId(entity.getId());
        listing.setSellerId(entity.getSellerId());
        listing.setTitle(entity.getTitle());
        listing.setDescription(entity.getDescription());
        listing.setPrice(new ListingPrice(entity.getPriceAmount(), entity.getPriceCurrency()));
        listing.setCapacity(new Capacity(entity.getCapacityValue(), entity.getCapacityUnit()));
        listing.setStatus(entity.getStatus());
        listing.setReservationReference(entity.getReservationReference());
        listing.setCreatedAt(entity.getCreatedAt());
        listing.setUpdatedAt(entity.getUpdatedAt());
        return listing;
    }
}

