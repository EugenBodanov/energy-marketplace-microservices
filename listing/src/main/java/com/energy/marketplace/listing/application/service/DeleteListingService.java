package com.energy.marketplace.listing.application.service;

import com.energy.marketplace.listing.application.command.DeleteListingCommand;
import com.energy.marketplace.listing.application.port.in.DeleteListingUseCase;
import com.energy.marketplace.listing.application.port.out.DeleteListingPort;
import com.energy.marketplace.listing.application.port.out.LoadListingPort;
import com.energy.marketplace.listing.domain.exception.ListingNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteListingService implements DeleteListingUseCase {

    private final LoadListingPort loadListingPort;
    private final DeleteListingPort deleteListingPort;

    @Override
    public void delete(DeleteListingCommand command) {
        loadListingPort.findById(command.listingId())
                .orElseThrow(() -> new ListingNotFoundException(
                        "Listing not found with id: " + command.listingId()
                ));

        deleteListingPort.deleteById(command.listingId());
    }
}

