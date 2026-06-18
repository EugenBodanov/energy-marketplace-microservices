package com.energy.marketplace.listing.application.port.out;

import com.energy.marketplace.listing.application.event.*;

public interface PublishListingEventPort {
    void publishListingCreated(ListingCreatedEvent event);
    void publishListingReserved(ListingReservedEvent event);
    void publishListingReservationFailed(ListingReservationFailedEvent event);
    void publishListingReleased(ListingReleasedEvent event);
    void publishListingClosed(ListingClosedEvent event);
    void publishListingCloseFailed(ListingCloseFailedEvent event);
    void publishListingCancelled(ListingCancelledEvent event);
    void publishListingCompensationFailed(ListingCompensationFailedEvent event);
}
