package com.energy.marketplace.listing.adapter.in.web.dto;

import jakarta.validation.constraints.*;

public record CreateListingRequest(
        @NotNull(message = "Seller ID is required")
        Long sellerId,

        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
        String description,

        @NotNull(message = "Price amount is required")
        @Positive(message = "Price amount must be positive")
        Double priceAmount,

        @NotBlank(message = "Price currency is required")
        String priceCurrency,

        @NotNull(message = "Capacity value is required")
        @Positive(message = "Capacity value must be positive")
        Double capacityValue,

        @NotBlank(message = "Capacity unit is required")
        String capacityUnit
) {
}

