package com.energy.marketplace.listing.config;

import com.energy.marketplace.listing.adapter.in.web.mapper.ListingWebMapper;
import com.energy.marketplace.listing.adapter.out.persistence.ListingJpaRepository;
import com.energy.marketplace.listing.adapter.out.persistence.ListingPersistenceAdapter;
import com.energy.marketplace.listing.adapter.out.persistence.ListingPersistenceMapper;
import com.energy.marketplace.listing.application.port.out.*;
import com.energy.marketplace.listing.application.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ListingBeanConfig {

    // Persistence Adapter
    @Bean
    public ListingPersistenceMapper listingPersistenceMapper() {
        return new ListingPersistenceMapper();
    }

    @Bean
    public ListingPersistenceAdapter listingPersistenceAdapter(
            ListingJpaRepository listingJpaRepository,
            ListingPersistenceMapper listingPersistenceMapper
    ) {
        return new ListingPersistenceAdapter(listingJpaRepository, listingPersistenceMapper);
    }

    // Web Mapper
    @Bean
    public ListingWebMapper listingWebMapper() {
        return new ListingWebMapper();
    }

    // Use Case Services
    @Bean
    public CreateListingService createListingService(
            SaveListingPort saveListingPort,
            PublishListingEventPort publishListingEventPort
    ) {
        return new CreateListingService(saveListingPort, publishListingEventPort);
    }

    @Bean
    public UpdateListingService updateListingService(
            LoadListingPort loadListingPort,
            SaveListingPort saveListingPort
    ) {
        return new UpdateListingService(loadListingPort, saveListingPort);
    }

    @Bean
    public DeleteListingService deleteListingService(
            LoadListingPort loadListingPort,
            DeleteListingPort deleteListingPort
    ) {
        return new DeleteListingService(loadListingPort, deleteListingPort);
    }

    @Bean
    public GetListingService getListingService(
            LoadListingPort loadListingPort
    ) {
        return new GetListingService(loadListingPort);
    }

    @Bean
    public SearchListingsService searchListingsService(
            SearchListingsPort searchListingsPort
    ) {
        return new SearchListingsService(searchListingsPort);
    }

    @Bean
    public ReserveListingService reserveListingService(
            LoadListingPort loadListingPort,
            SaveListingPort saveListingPort,
            PublishListingEventPort publishListingEventPort
    ) {
        return new ReserveListingService(loadListingPort, saveListingPort, publishListingEventPort);
    }

    @Bean
    public ReleaseListingService releaseListingService(
            LoadListingPort loadListingPort,
            SaveListingPort saveListingPort,
            PublishListingEventPort publishListingEventPort
    ) {
        return new ReleaseListingService(loadListingPort, saveListingPort, publishListingEventPort);
    }

    @Bean
    public CloseListingService closeListingService(
            LoadListingPort loadListingPort,
            SaveListingPort saveListingPort,
            PublishListingEventPort publishListingEventPort
    ) {
        return new CloseListingService(loadListingPort, saveListingPort, publishListingEventPort);
    }
}

