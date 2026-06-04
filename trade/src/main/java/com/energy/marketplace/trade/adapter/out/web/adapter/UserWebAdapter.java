package com.energy.marketplace.trade.adapter.out.web.adapter;

import com.energy.marketplace.trade.adapter.out.web.dto.UserValidateResponse;
import com.energy.marketplace.trade.adapter.out.web.exception.UserClientException;
import com.energy.marketplace.trade.application.port.out.ValidateTradeParticipantsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserWebAdapter extends Adapter implements ValidateTradeParticipantsPort {

    private final RestClient userRestClient;

    @Override
    public boolean validateBuyer(Long buyerId) {
        return validateUser(buyerId);
    }

    @Override
    public boolean validateSeller(Long sellerId) {
        return validateUser(sellerId);
    }

    private boolean validateUser(Long userId) {
        UserValidateResponse response = userRestClient.get()
                .uri("/users/{userId}/validate?purpose=PARTICIPATE_IN_TRADE", userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                    throw new UserClientException(
                            "User Service returned error while validating user userId=%d. Status=%s. Body=%s"
                                    .formatted(
                                            userId,
                                            clientResponse.getStatusCode(),
                                            readBody(clientResponse)
                                    )
                    );
                })
                .body(UserValidateResponse.class);

        return Objects.requireNonNull(response).valid();
    }
}
