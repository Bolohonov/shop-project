package com.shop.payment.repository;

import com.shop.payment.entity.PaymentTransaction;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface PaymentTransactionRepository extends CrudRepository<PaymentTransaction, UUID> {}
