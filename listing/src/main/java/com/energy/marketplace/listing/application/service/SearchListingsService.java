package com.energy.marketplace.listing.application.service;

import com.energy.marketplace.listing.application.port.in.SearchListingsUseCase;
import com.energy.marketplace.listing.application.port.out.SearchListingsPort;
import com.energy.marketplace.listing.application.result.ListingResult;
import com.energy.marketplace.listing.application.result.SearchListingsResult;
import com.energy.marketplace.listing.domain.model.Listing;
import com.energy.marketplace.listing.domain.valueObject.ListingStatus;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SearchListingsService implements SearchListingsUseCase {

    private final SearchListingsPort searchListingsPort;

    @Override
    public SearchListingsResult searchAll(Integer page, Integer pageSize) {
        List<Listing> listings = searchListingsPort.findAll();
        return paginate(listings, page, pageSize);
    }

    @Override
    public SearchListingsResult searchByStatus(String status, Integer page, Integer pageSize) {
        ListingStatus listingStatus = ListingStatus.valueOf(status.toUpperCase());
        List<Listing> listings = searchListingsPort.findByStatus(listingStatus);
        return paginate(listings, page, pageSize);
    }

    @Override
    public SearchListingsResult searchBySellerId(Long sellerId, Integer page, Integer pageSize) {
        List<Listing> listings = searchListingsPort.findBySellerId(sellerId);
        return paginate(listings, page, pageSize);
    }

    private SearchListingsResult paginate(List<Listing> listings, Integer page, Integer pageSize) {
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, listings.size());

        List<ListingResult> paginatedResults = listings.subList(startIndex, endIndex)
                .stream()
                .map(this::mapToResult)
                .toList();

        return new SearchListingsResult(
                paginatedResults,
                (long) listings.size(),
                page,
                pageSize
        );
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

