package com.energy.marketplace.listing.application.service;

import com.energy.marketplace.listing.application.command.ReleaseListingCommand;
import com.energy.marketplace.listing.application.event.ListingReleasedEvent;
import com.energy.marketplace.listing.application.port.in.ReleaseListingUseCase;
import com.energy.marketplace.listing.application.port.out.LoadListingPort;
import com.energy.marketplace.listing.application.port.out.PublishListingEventPort;
import com.energy.marketplace.listing.application.port.out.SaveListingPort;
import com.energy.marketplace.listing.application.result.ReservationResult;
import com.energy.marketplace.listing.domain.exception.ListingInvalidStateException;
import com.energy.marketplace.listing.domain.exception.ListingNotFoundException;
import com.energy.marketplace.listing.domain.model.Listing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReleaseListingService implements ReleaseListingUseCase {

    private final LoadListingPort loadListingPort;
    private final SaveListingPort saveListingPort;
    private final PublishListingEventPort publishListingEventPort;

    @Override
    public ReservationResult release(ReleaseListingCommand command) {
        try {
            Listing listing = loadListingPort.findById(command.listingId())
                    .orElseThrow(() -> new ListingNotFoundException(
                            "Listing not found with id: " + command.listingId()
                    ));

            String previousStatus = listing.getStatus().name();
            listing.release();

            Listing savedListing = saveListingPort.save(listing);

            publishListingEventPort.publishListingReleased(
                    ListingReleasedEvent.now(command.listingId(), command.tradeId())
            );

            log.info("Listing {} released successfully from trade {}", command.listingId(), command.tradeId());

            return ReservationResult.success(
                    command.listingId(),
                    previousStatus,
                    savedListing.getStatus().name(),
                    null
            );
        } catch (ListingNotFoundException | ListingInvalidStateException e) {
            String errorMessage = e.getMessage();
            log.warn("Failed to release listing {}: {}", command.listingId(), errorMessage);
            return ReservationResult.failure(command.listingId(), errorMessage);
        }
    }
}

