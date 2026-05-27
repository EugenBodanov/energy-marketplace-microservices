package com.energy.marketplace.trade.adapter.in.messaging.listener;

import tools.jackson.databind.JsonNode;

public class Listener {
    protected String readEventType(JsonNode root) {
        String eventType = root.path("eventType").asText(null);

        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("Missing eventType in billing event");
        }

        return eventType;
    }
}
