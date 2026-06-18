package com.energy.marketplace.listing.application.service;

import com.energy.marketplace.listing.application.command.CloseListingCommand;
import com.energy.marketplace.listing.application.event.ListingCloseFailedEvent;
import com.energy.marketplace.listing.application.event.ListingClosedEvent;
import com.energy.marketplace.listing.application.port.in.CloseListingUseCase;
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
public class CloseListingService implements CloseListingUseCase {

    private final LoadListingPort loadListingPort;
    private final SaveListingPort saveListingPort;
    private final PublishListingEventPort publishListingEventPort;

    @Override
    public ReservationResult close(CloseListingCommand command) {
        try {
            Listing listing = loadListingPort.findById(command.listingId())
                    .orElseThrow(() -> new ListingNotFoundException(
                            "Listing not found with id: " + command.listingId()
                    ));

            String previousStatus = listing.getStatus().name();
            listing.close();

            Listing savedListing = saveListingPort.save(listing);

            publishListingEventPort.publishListingClosed(
                    ListingClosedEvent.now(command.listingId(), command.tradeId())
            );

            log.info("Listing {} closed successfully for trade {}", command.listingId(), command.tradeId());

            return ReservationResult.success(
                    command.listingId(),
                    previousStatus,
                    savedListing.getStatus().name(),
                    savedListing.getReservationReference()
            );
        } catch (ListingNotFoundException | ListingInvalidStateException e) {
            String errorMessage = e.getMessage();
            log.warn("Failed to close listing {}: {}", command.listingId(), errorMessage);
            publishListingEventPort.publishListingCloseFailed(
                    ListingCloseFailedEvent.now(command.listingId(), command.tradeId(), errorMessage)
            );
            return ReservationResult.failure(command.listingId(), errorMessage);
        }
    }
}

