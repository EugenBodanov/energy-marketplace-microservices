package com.energy.marketplace.listing.application.service;

import com.energy.marketplace.listing.application.command.UpdateListingCommand;
import com.energy.marketplace.listing.application.port.in.UpdateListingUseCase;
import com.energy.marketplace.listing.application.port.out.LoadListingPort;
import com.energy.marketplace.listing.application.port.out.SaveListingPort;
import com.energy.marketplace.listing.application.result.ListingResult;
import com.energy.marketplace.listing.domain.exception.ListingNotFoundException;
import com.energy.marketplace.listing.domain.model.Listing;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateListingService implements UpdateListingUseCase {

    private final LoadListingPort loadListingPort;
    private final SaveListingPort saveListingPort;

    @Override
    public ListingResult update(UpdateListingCommand command) {
        Listing listing = loadListingPort.findById(command.listingId())
                .orElseThrow(() -> new ListingNotFoundException(
                        "Listing not found with id: " + command.listingId()
                ));

        listing.update(
                command.title(),
                command.description(),
                command.getPrice(),
                command.getCapacity()
        );

        Listing updatedListing = saveListingPort.save(listing);

        return mapToResult(updatedListing);
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

