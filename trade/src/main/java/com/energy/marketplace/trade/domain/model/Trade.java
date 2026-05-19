package com.energy.marketplace.trade.domain.model;

import com.energy.marketplace.trade.domain.exception.InvalidTradeStateException;
import com.energy.marketplace.trade.domain.valueobject.Money;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@lombok.Getter
public class Trade {

    @PositiveOrZero(message = "Trade id must be positive or zero")
    private Long id;

    @NotNull(message = "Buyer id must not be null")
    private Long buyerId;

    @PositiveOrZero(message = "Seller id must be positive or zero")
    @NotNull(message = "Seller id must not be null")
    private Long sellerId;

    @PositiveOrZero(message = "Listing id must be positive or zero")
    @NotNull(message = "Listing id must not be null")
    private Long listingId;

    @Positive(message = "Trade amount must be positive")
    @NotNull(message = "Trade amount must not be null")
    private Money amount;

    @NotNull(message = "Trade status must not be null")
    private TradeStatus status;

    @PositiveOrZero(message = "Receipt id must be positive or zero")
    @NotNull(message = "Receipt id must not be null")
    private Long paymentAuthorizationId;

    @PositiveOrZero(message = "Receipt id must be positive or zero")
    @NotNull(message = "Receipt id must not be null")
    private Long receiptId;

    public Trade(
            Long id,
            Long buyerId,
            Long sellerId,
            Long listingId,
            Money amount,
            TradeStatus status
    ) {
        this.id = id;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.listingId = listingId;
        this.amount = amount;
        this.status = status;

        if (buyerId.equals(sellerId)) {
            throw new IllegalArgumentException("Buyer and seller must be different users");
        }
    }

    public static Trade createTrade(
            Long buyerId,
            Long sellerId,
            Long listingId,
            Money amount
    ) {
        return new Trade(
                null,
                buyerId,
                sellerId,
                listingId,
                amount,
                TradeStatus.CREATED
        );
    }

    public void markUserValidationPending() {
        changeStatus(
                TradeStatus.CREATED,
                TradeStatus.USER_VALIDATION_PENDING
        );
    }

    public void markUserValidated() {
        changeStatus(
                TradeStatus.USER_VALIDATION_PENDING,
                TradeStatus.USER_VALIDATED
        );
    }

    public void markListingReservationPending() {
        changeStatus(
                TradeStatus.USER_VALIDATED,
                TradeStatus.LISTING_RESERVATION_PENDING
        );
    }

    public void markListingReserved() {
        changeStatus(
                TradeStatus.LISTING_RESERVATION_PENDING,
                TradeStatus.LISTING_RESERVED
        );
    }

    public void markPaymentAuthorizationPending() {
        changeStatus(
                TradeStatus.LISTING_RESERVED,
                TradeStatus.PAYMENT_AUTHORIZATION_PENDING
        );
    }

    public void recordPaymentAuthorization(Long paymentAuthorizationId) {
        this.paymentAuthorizationId = paymentAuthorizationId;
    }

    public void markPaymentAuthorized() {
        changeStatus(
                TradeStatus.PAYMENT_AUTHORIZATION_PENDING,
                TradeStatus.PAYMENT_AUTHORIZED
        );
    }

    public void markListingClosingPending() {
        changeStatus(
                TradeStatus.PAYMENT_AUTHORIZED,
                TradeStatus.LISTING_CLOSING_PENDING
        );
    }

    public void markListingClosed() {
        changeStatus(
                TradeStatus.LISTING_CLOSING_PENDING,
                TradeStatus.LISTING_CLOSED
        );
    }

    public void markPaymentSettlementPending() {
        changeStatus(
                TradeStatus.LISTING_CLOSED,
                TradeStatus.PAYMENT_SETTLEMENT_PENDING
        );
    }

    public void markPaymentSettled() {
        changeStatus(
                TradeStatus.PAYMENT_SETTLEMENT_PENDING,
                TradeStatus.PAYMENT_SETTLED
        );
    }

    public void markPaymentRollbackPendingFromListingClosingFailure() {
        changeStatus(
                TradeStatus.LISTING_CLOSING_PENDING,
                TradeStatus.PAYMENT_ROLLBACK_PENDING
        );
    }

    public void markPaymentRollbackPendingFromSettlementFailure() {
        changeStatus(
                TradeStatus.PAYMENT_SETTLEMENT_PENDING,
                TradeStatus.PAYMENT_ROLLBACK_PENDING
        );
    }

    public void markListingCompensationPendingFromPaymentAuthorizationFailure() {
        changeStatus(
                TradeStatus.PAYMENT_AUTHORIZATION_PENDING,
                TradeStatus.LISTING_COMPENSATION_PENDING
        );
    }

    public void markListingCompensationPendingAfterPaymentRollback() {
        changeStatus(
                TradeStatus.PAYMENT_ROLLBACK_PENDING,
                TradeStatus.LISTING_COMPENSATION_PENDING
        );
    }

    public void markCompletedReceiptPending() {
        changeStatus(
                TradeStatus.PAYMENT_SETTLED,
                TradeStatus.COMPLETED_RECEIPT_PENDING
        );
    }

    public void markReceiptGenerationPending() {
        changeStatus(
                TradeStatus.COMPLETED_RECEIPT_PENDING,
                TradeStatus.RECEIPT_GENERATION_PENDING
        );
    }

    public void markReceiptRetryPending() {
        changeStatus(
                TradeStatus.RECEIPT_GENERATION_PENDING,
                TradeStatus.COMPLETED_RECEIPT_PENDING
        );
    }

    public void markCompleted() {
        changeStatus(
                TradeStatus.RECEIPT_GENERATION_PENDING,
                TradeStatus.COMPLETED
        );
    }

    public void markFailedFromUserValidation() {
        changeStatus(
                TradeStatus.USER_VALIDATION_PENDING,
                TradeStatus.FAILED
        );
    }

    public void markFailedFromListingReservation() {
        changeStatus(
                TradeStatus.LISTING_RESERVATION_PENDING,
                TradeStatus.FAILED
        );
    }

    public void markFailedAfterListingCompensation() {
        changeStatus(
                TradeStatus.LISTING_COMPENSATION_PENDING,
                TradeStatus.FAILED
        );
    }

    public void markCompensationFailedFromPaymentRollback() {
        changeStatus(
                TradeStatus.PAYMENT_ROLLBACK_PENDING,
                TradeStatus.COMPENSATION_FAILED
        );
    }

    public void markCompensationFailedFromListingCompensation() {
        changeStatus(
                TradeStatus.LISTING_COMPENSATION_PENDING,
                TradeStatus.COMPENSATION_FAILED
        );
    }

    public boolean isTerminal() {
        return status == TradeStatus.COMPLETED
                || status == TradeStatus.FAILED
                || status == TradeStatus.COMPENSATION_FAILED;
    }

    public boolean isCompleted() {
        return status == TradeStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == TradeStatus.FAILED
                || status == TradeStatus.COMPENSATION_FAILED;
    }

    private void changeStatus(
            TradeStatus expectedCurrentStatus,
            TradeStatus newStatus
    ) {
        if (this.status != expectedCurrentStatus) {
            throw new InvalidTradeStateException(
                    "Invalid trade state transition from "
                            + this.status
                            + " to "
                            + newStatus
                            + ". Expected current status: "
                            + expectedCurrentStatus
            );
        }

        this.status = newStatus;
    }

    public void recordReceipt(@NotNull(message = "Receipt id must not be null") @Positive(message = "Receipt id must be positive") Long receiptId) {
        this.receiptId = receiptId;
    }
}