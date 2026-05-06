package com.energy.marketplace.listing.adapter.in.web.mapper;

import com.energy.marketplace.listing.adapter.in.web.dto.*;
import com.energy.marketplace.listing.application.command.CreateListingCommand;
import com.energy.marketplace.listing.application.command.UpdateListingCommand;
import com.energy.marketplace.listing.application.result.ListingResult;
import com.energy.marketplace.listing.application.result.ReservationResult;
import com.energy.marketplace.listing.application.result.SearchListingsResult;

public class ListingWebMapper {

    public CreateListingCommand toCreateCommand(CreateListingRequest request) {
        return new CreateListingCommand(
                request.sellerId(),
                request.title(),
                request.description(),
                request.priceAmount(),
                request.priceCurrency(),
                request.capacityValue(),
                request.capacityUnit()
        );
    }

    public UpdateListingCommand toUpdateCommand(Long listingId, UpdateListingRequest request) {
        return new UpdateListingCommand(
                listingId,
                request.title(),
                request.description(),
                request.priceAmount(),
                request.priceCurrency(),
                request.capacityValue(),
                request.capacityUnit()
        );
    }

    public ListingResponse toResponse(ListingResult result) {
        return new ListingResponse(
                result.id(),
                result.sellerId(),
                result.title(),
                result.description(),
                result.priceAmount(),
                result.priceCurrency(),
                result.capacityValue(),
                result.capacityUnit(),
                result.status(),
                result.reservationReference(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    public SearchListingsResponse toResponse(SearchListingsResult result) {
        return new SearchListingsResponse(
                result.listings().stream()
                        .map(this::toResponse)
                        .toList(),
                result.total(),
                result.page(),
                result.pageSize()
        );
    }

    public ReservationResponse toResponse(ReservationResult result) {
        return new ReservationResponse(
                result.listingId(),
                result.previousStatus(),
                result.newStatus(),
                result.reservationReference(),
                result.success(),
                result.errorMessage()
        );
    }
}

