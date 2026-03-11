package com.shop.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис сброса демо-данных магазина.
 *
 * В отличие от CRM, shop не использует schema-per-tenant —
 * всё в одной схеме public. Поэтому вместо DROP + пересоздания
 * просто чистим данные демо-пользователя и восстанавливаем баланс.
 *
 * Вызывается из DemoResetController по внутреннему endpoint-у.
 * K8s CronJob дёргает его раз в ночь через curl — синхронно с CRM reset.
 *
 * Порядок важен из-за FK:
 *   order_items, order_status_history → orders → payment_transactions
 *   затем cart_items, user_balances
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoResetService {

    public static final String DEMO_USER_ID = "dddddddd-0000-0000-0000-000000000001";
    private static final long  DEMO_BALANCE  = 1_000_000L;

    private final JdbcTemplate jdbc;

    @Transactional
    public void reset() {
        log.info("Shop demo reset started for user {}", DEMO_USER_ID);
        long start = System.currentTimeMillis();

        // 1. Корзина
        int cart = jdbc.update(
                "DELETE FROM cart_items WHERE user_id = ?::uuid", DEMO_USER_ID);
        log.debug("Deleted {} cart_items", cart);

        // 2. Позиции заказов и история статусов (cascade по FK, но явно надёжнее)
        jdbc.update("""
            DELETE FROM order_items
            WHERE order_id IN (SELECT id FROM orders WHERE user_id = ?::uuid)
            """, DEMO_USER_ID);

        jdbc.update("""
            DELETE FROM order_status_history
            WHERE order_id IN (SELECT id FROM orders WHERE user_id = ?::uuid)
            """, DEMO_USER_ID);

        // 3. Транзакции оплат
        jdbc.update(
                "DELETE FROM payment_transactions WHERE user_id = ?::uuid", DEMO_USER_ID);

        // 4. Сами заказы
        int orders = jdbc.update(
                "DELETE FROM orders WHERE user_id = ?::uuid", DEMO_USER_ID);
        log.debug("Deleted {} orders", orders);

        // 5. Восстанавливаем баланс
        jdbc.update("""
            UPDATE user_balances SET balance = ?, updated_at = NOW()
            WHERE user_id = ?::uuid
            """, DEMO_BALANCE, DEMO_USER_ID);
        log.debug("Balance restored to {}", DEMO_BALANCE);

        long elapsed = System.currentTimeMillis() - start;
        log.info("Shop demo reset completed in {}ms", elapsed);
    }
}
