package com.energy.marketplace.listing.adapter.out.persistence;

import com.energy.marketplace.listing.application.port.out.DeleteListingPort;
import com.energy.marketplace.listing.application.port.out.LoadListingPort;
import com.energy.marketplace.listing.application.port.out.SaveListingPort;
import com.energy.marketplace.listing.application.port.out.SearchListingsPort;
import com.energy.marketplace.listing.domain.model.Listing;
import com.energy.marketplace.listing.domain.valueObject.ListingStatus;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ListingPersistenceAdapter implements LoadListingPort, SaveListingPort, SearchListingsPort, DeleteListingPort {

    private final ListingJpaRepository listingJpaRepository;
    private final ListingPersistenceMapper listingPersistenceMapper;

    @Override
    public Optional<Listing> findById(Long listingId) {
        return listingJpaRepository.findById(listingId)
                .map(listingPersistenceMapper::toDomain);
    }

    @Override
    public Listing save(Listing listing) {
        ListingJpaEntity entity = listingPersistenceMapper.toEntity(listing);
        ListingJpaEntity savedEntity = listingJpaRepository.save(entity);
        return listingPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public List<Listing> findAll() {
        return listingJpaRepository.findAll()
                .stream()
                .map(listingPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Listing> findByStatus(ListingStatus status) {
        return listingJpaRepository.findByStatus(status)
                .stream()
                .map(listingPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Listing> findBySellerId(Long sellerId) {
        return listingJpaRepository.findBySellerId(sellerId)
                .stream()
                .map(listingPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long listingId) {
        listingJpaRepository.deleteById(listingId);
    }
}

