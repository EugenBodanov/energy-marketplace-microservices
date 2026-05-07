package com.energy.marketplace.listing.application.service;

import com.energy.marketplace.listing.application.command.ReserveListingCommand;
import com.energy.marketplace.listing.application.event.ListingReservationFailedEvent;
import com.energy.marketplace.listing.application.event.ListingReservedEvent;
import com.energy.marketplace.listing.application.port.in.ReserveListingUseCase;
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
public class ReserveListingService implements ReserveListingUseCase {

    private final LoadListingPort loadListingPort;
    private final SaveListingPort saveListingPort;
    private final PublishListingEventPort publishListingEventPort;

    @Override
    public ReservationResult reserve(ReserveListingCommand command) {
        try {
            Listing listing = loadListingPort.findById(command.listingId())
                    .orElseThrow(() -> new ListingNotFoundException(
                            "Listing not found with id: " + command.listingId()
                    ));

            String previousStatus = listing.getStatus().name();
            listing.reserve(command.tradeId());

            Listing savedListing = saveListingPort.save(listing);

            publishListingEventPort.publishListingReserved(
                    ListingReservedEvent.now(command.listingId(), command.tradeId())
            );

            log.info("Listing {} reserved successfully for trade {}", command.listingId(), command.tradeId());

            return ReservationResult.success(
                    command.listingId(),
                    previousStatus,
                    savedListing.getStatus().name(),
                    savedListing.getReservationReference()
            );
        } catch (ListingNotFoundException | ListingInvalidStateException e) {
            String errorMessage = e.getMessage();
            log.warn("Failed to reserve listing {}: {}", command.listingId(), errorMessage);

            publishListingEventPort.publishListingReservationFailed(
                    ListingReservationFailedEvent.now(command.listingId(), command.tradeId(), errorMessage)
            );

            return ReservationResult.failure(command.listingId(), errorMessage);
        }
    }
}

