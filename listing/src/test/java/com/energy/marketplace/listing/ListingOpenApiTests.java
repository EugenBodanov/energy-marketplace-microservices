package com.energy.marketplace.listing;

import com.energy.marketplace.listing.adapter.in.web.ListingController;
import com.energy.marketplace.listing.adapter.in.web.mapper.ListingWebMapper;
import com.energy.marketplace.listing.application.port.in.CreateListingUseCase;
import com.energy.marketplace.listing.application.port.in.DeleteListingUseCase;
import com.energy.marketplace.listing.application.port.in.GetListingUseCase;
import com.energy.marketplace.listing.application.port.in.SearchListingsUseCase;
import com.energy.marketplace.listing.application.port.in.UpdateListingUseCase;
import com.energy.marketplace.listing.config.OpenApiConfig;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.MultipleOpenApiSupportConfiguration;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ListingController.class, properties = "server.servlet.context-path=")
@ImportAutoConfiguration({
        SpringDocConfiguration.class,
        SpringDocConfigProperties.class,
        SpringDocWebMvcConfiguration.class,
        MultipleOpenApiSupportConfiguration.class
})
@Import(OpenApiConfig.class)
class ListingOpenApiTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateListingUseCase createListingUseCase;

    @MockitoBean
    private UpdateListingUseCase updateListingUseCase;

    @MockitoBean
    private DeleteListingUseCase deleteListingUseCase;

    @MockitoBean
    private GetListingUseCase getListingUseCase;

    @MockitoBean
    private SearchListingsUseCase searchListingsUseCase;

    @MockitoBean
    private ListingWebMapper listingWebMapper;

    @Test
    void exposesListingServiceOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/listings/v3/api-docs/listing-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Listing Service API"))
                .andExpect(jsonPath("$.paths['/listings'].post.operationId").value("createListing"))
                .andExpect(jsonPath("$.paths['/listings/{listingId}'].get.parameters[0].name").value("listingId"))
                .andExpect(jsonPath("$.paths['/listings'].get.parameters[3].schema.default").value("10"))
                .andExpect(jsonPath("$.components.schemas.CreateListingRequest.properties.title.minLength").value(3));
    }
}
