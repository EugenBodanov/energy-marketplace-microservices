package com.energy.marketplace.listing.application.result;

import java.util.List;

public record SearchListingsResult(
        List<ListingResult> listings,
        Long total,
        Integer page,
        Integer pageSize
) {
}

