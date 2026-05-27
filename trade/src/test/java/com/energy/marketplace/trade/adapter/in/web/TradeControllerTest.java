package com.energy.marketplace.trade.adapter.in.web;

import com.energy.marketplace.trade.adapter.in.web.dto.CreateTradeRequest;
import com.energy.marketplace.trade.adapter.in.web.dto.CreateTradeResponse;
import com.energy.marketplace.trade.adapter.in.web.dto.GetTradeResponse;
import com.energy.marketplace.trade.application.port.in.CreateTradeUseCase;
import com.energy.marketplace.trade.application.result.CreateTradeResult;
import com.energy.marketplace.trade.application.result.GetTradeResult;
import com.energy.marketplace.trade.application.service.CreateTradeService;
import com.energy.marketplace.trade.application.service.GetReceiptService;
import com.energy.marketplace.trade.application.service.GetTradeService;
import com.energy.marketplace.trade.domain.model.TradeStatus;
import com.energy.marketplace.trade.domain.valueObject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TradeController.class)
@Import({TradeWebMapper.class})
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private JsonMapper jsonMapper = new JsonMapper();

    @MockitoBean
    private CreateTradeService createTradeService;

    @MockitoBean
    private TradeWebMapper tradeWebMapper;

    @MockitoBean
    private GetTradeService getTradeService;

    @MockitoBean
    private GetReceiptService getReceiptService;

    @Test
    @DisplayName("Should return 200 OK and trade info when trade is created")
    void shouldCreateTrade() throws Exception {
        CreateTradeRequest request = new CreateTradeRequest(1L, 2L, 3L, new BigDecimal("100.00"), "EUR");
        CreateTradeResult result = new CreateTradeResult(100L, TradeStatus.CREATED);
        CreateTradeResponse response = new CreateTradeResponse(100L, TradeStatus.CREATED);

        when(tradeWebMapper.toCreateTradeCommand(any())).thenReturn(null); // command is not used in mock
        when(createTradeService.createTrade(any())).thenReturn(result);
        when(tradeWebMapper.toCreateTradeResponse(result)).thenReturn(response);

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId").value(100L))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @DisplayName("Should return trade info by id")
    void shouldGetTrade() throws Exception {
        Long tradeId = 100L;
        GetTradeResult result = new GetTradeResult(tradeId, 1L, 2L, 3L, Money.of("100.00", "EUR"), TradeStatus.CREATED);
        GetTradeResponse response = new GetTradeResponse(tradeId, 1L, 2L, 3L, new BigDecimal("100.00"), "EUR", TradeStatus.CREATED);

        when(tradeWebMapper.toGetTradeCommand(tradeId)).thenReturn(null);
        when(getTradeService.getTrade(any())).thenReturn(result);
        when(tradeWebMapper.toGetTradeResponse(result)).thenReturn(response);

        mockMvc.perform(get("/trades/{tradeId}", tradeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId").value(tradeId))
                .andExpect(jsonPath("$.buyerId").value(1L))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }
}
