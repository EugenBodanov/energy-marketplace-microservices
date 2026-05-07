package com.energy.marketplace.listing.application.service;

import com.energy.marketplace.listing.application.command.CreateListingCommand;
import com.energy.marketplace.listing.application.event.ListingCreatedEvent;
import com.energy.marketplace.listing.application.port.in.CreateListingUseCase;
import com.energy.marketplace.listing.application.port.out.PublishListingEventPort;
import com.energy.marketplace.listing.application.port.out.SaveListingPort;
import com.energy.marketplace.listing.application.result.ListingResult;
import com.energy.marketplace.listing.domain.model.Listing;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateListingService implements CreateListingUseCase {

    private final SaveListingPort saveListingPort;
    private final PublishListingEventPort publishListingEventPort;

    @Override
    public ListingResult create(CreateListingCommand command) {
        Listing listing = Listing.create(
                command.sellerId(),
                command.title(),
                command.description(),
                command.getPrice(),
                command.getCapacity()
        );

        Listing savedListing = saveListingPort.save(listing);

        publishListingEventPort.publishListingCreated(
                ListingCreatedEvent.now(
                        savedListing.getId(),
                        savedListing.getSellerId(),
                        savedListing.getTitle(),
                        savedListing.getPrice().getAmount(),
                        savedListing.getPrice().getCurrency(),
                        savedListing.getCapacity().getValue(),
                        savedListing.getCapacity().getUnit()
                )
        );

        return mapToResult(savedListing);
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

