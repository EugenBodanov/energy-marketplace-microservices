package com.energy.marketplace.listing.adapter.in.web;

import com.energy.marketplace.listing.adapter.in.web.dto.*;
import com.energy.marketplace.listing.adapter.in.web.mapper.ListingWebMapper;
import com.energy.marketplace.listing.application.command.DeleteListingCommand;
import com.energy.marketplace.listing.application.port.in.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/listings")
@RequiredArgsConstructor
public class ListingController {

    private final CreateListingUseCase createListingUseCase;
    private final UpdateListingUseCase updateListingUseCase;
    private final DeleteListingUseCase deleteListingUseCase;
    private final GetListingUseCase getListingUseCase;
    private final SearchListingsUseCase searchListingsUseCase;
    private final ListingWebMapper listingWebMapper;

    @PostMapping
    public ResponseEntity<ListingResponse> createListing(@Valid @RequestBody CreateListingRequest request) {
        var command = listingWebMapper.toCreateCommand(request);
        var result = createListingUseCase.create(command);
        var response = listingWebMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{listingId}")
    public ResponseEntity<ListingResponse> updateListing(
            @PathVariable("listingId") Long listingId,
            @Valid @RequestBody UpdateListingRequest request
    ) {
        var command = listingWebMapper.toUpdateCommand(listingId, request);
        var result = updateListingUseCase.update(command);
        var response = listingWebMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<Void> deleteListing(@PathVariable("listingId") Long listingId) {
        var command = new DeleteListingCommand(listingId);
        deleteListingUseCase.delete(command);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{listingId}")
    public ResponseEntity<ListingResponse> getListing(@PathVariable("listingId") Long listingId) {
        var result = getListingUseCase.getById(listingId);
        var response = listingWebMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<SearchListingsResponse> searchListings(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "sellerId", required = false) Long sellerId,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        var result = (status != null)
                ? searchListingsUseCase.searchByStatus(status, page, pageSize)
                : (sellerId != null)
                ? searchListingsUseCase.searchBySellerId(sellerId, page, pageSize)
                : searchListingsUseCase.searchAll(page, pageSize);

        var response = listingWebMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }
}

