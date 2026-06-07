package com.handynest.payment;

public interface PaymentGateway {

    PaymentGatewayReference createPayment(PaymentTransaction transaction);
}
