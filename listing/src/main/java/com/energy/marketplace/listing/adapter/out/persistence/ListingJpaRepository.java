package com.energy.marketplace.listing.adapter.out.persistence;

import com.energy.marketplace.listing.domain.valueObject.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingJpaRepository extends JpaRepository<ListingJpaEntity, Long> {
    List<ListingJpaEntity> findByStatus(ListingStatus status);
    List<ListingJpaEntity> findBySellerId(Long sellerId);
}

