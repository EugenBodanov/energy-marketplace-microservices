package com.energy.marketplace.listing.adapter.in.web.dto;

import java.util.List;

public record SearchListingsResponse(
        List<ListingResponse> listings,
        Long total,
        Integer page,
        Integer pageSize
) {
}

