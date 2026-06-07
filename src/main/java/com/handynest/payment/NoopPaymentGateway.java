package com.handynest.payment;

import org.springframework.stereotype.Component;

@Component
public class NoopPaymentGateway implements PaymentGateway {

    private static final String PROVIDER = "HANDYNEST_MVP";

    @Override
    public PaymentGatewayReference createPayment(PaymentTransaction transaction) {
        return new PaymentGatewayReference(
                PROVIDER,
                "mvp_" + transaction.getPublicId(),
                "ref_" + transaction.getPublicId()
        );
    }
}
