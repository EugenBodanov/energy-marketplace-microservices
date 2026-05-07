package com.energy.marketplace.listing.application.service;

import com.energy.marketplace.listing.application.port.in.GetListingUseCase;
import com.energy.marketplace.listing.application.port.out.LoadListingPort;
import com.energy.marketplace.listing.application.result.ListingResult;
import com.energy.marketplace.listing.domain.exception.ListingNotFoundException;
import com.energy.marketplace.listing.domain.model.Listing;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetListingService implements GetListingUseCase {

    private final LoadListingPort loadListingPort;

    @Override
    public ListingResult getById(Long listingId) {
        Listing listing = loadListingPort.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(
                        "Listing not found with id: " + listingId
                ));

        return mapToResult(listing);
    }

    private ListingResult mapToResult(Listing listing) {
        return new ListingResult(
                listing.getId(),
                listing.getSellerId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice().getAmount(),
                listing.getPrice().getCurrency(),
                listing.getCapacity().getValue(),
                listing.getCapacity().getUnit(),
                listing.getStatus().name(),
                listing.getReservationReference(),
                listing.getCreatedAt(),
                listing.getUpdatedAt()
        );
    }
}

