package com.energy.marketplace.listing.application.port.in;

import com.energy.marketplace.listing.application.result.SearchListingsResult;

public interface SearchListingsUseCase {
    SearchListingsResult searchAll(Integer page, Integer pageSize);
    SearchListingsResult searchByStatus(String status, Integer page, Integer pageSize);
    SearchListingsResult searchBySellerId(Long sellerId, Integer page, Integer pageSize);
}

