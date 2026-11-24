package com.fitnessapp.wallet.repo;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class WalletAnalyticsRepository {

    private final JdbcTemplate jdbc;

    /**
     * Converts Instant into a JDBC-compatible Timestamp.
     */
    private Timestamp timestamp(Instant instant) {
        return Timestamp.from(instant);
    }

    /**
     * Restricts SQL date truncation fragments to supported values.
     */
    private String validatedDateTrunc(String dateTrunc) {
        return switch (dateTrunc) {
            case "day", "week", "month" -> dateTrunc;
            default -> throw new IllegalArgumentException(
                    "Unsupported date truncation interval: " + dateTrunc
            );
        };
    }

    // ---------------------------------------------------------
    // BALANCE ANALYTICS
    // ---------------------------------------------------------

    public Map<String, Object> balance() {
        return jdbc.queryForMap("""
                SELECT
                    COALESCE(SUM(balance_coins), 0) total_balance,
                    COUNT(*) FILTER (WHERE balance_coins > 0) positive_wallets
                FROM app_user_wallet_accounts
                """);
    }

    // ---------------------------------------------------------
    // TRANSACTION ANALYTICS
    // ---------------------------------------------------------

    public Map<String, Object> transactions(Instant from, Instant to) {

        Timestamp fromTimestamp = timestamp(from);
        Timestamp toTimestamp = timestamp(to);

        return jdbc.queryForMap("""
                        SELECT
                            COUNT(*) total,
                        
                            COUNT(*) FILTER (WHERE type = 'CREDIT') credits,
                            COUNT(*) FILTER (WHERE type = 'DEBIT') debits,
                        
                            COALESCE(
                                SUM(amount_coins) FILTER (WHERE type = 'CREDIT'),
                                0
                            ) credited,
                        
                            COALESCE(
                                SUM(amount_coins) FILTER (WHERE type = 'DEBIT'),
                                0
                            ) debited,
                        
                            COUNT(*) FILTER (
                                WHERE created_at >= ? AND created_at <= ?
                            ) period_total,
                        
                            COUNT(*) FILTER (
                                WHERE type = 'CREDIT'
                                AND created_at >= ? AND created_at <= ?
                            ) period_credits,
                        
                            COUNT(*) FILTER (
                                WHERE type = 'DEBIT'
                                AND created_at >= ? AND created_at <= ?
                            ) period_debits,
                        
                            COALESCE(
                                SUM(amount_coins) FILTER (
                                    WHERE type = 'CREDIT'
                                    AND created_at >= ? AND created_at <= ?
                                ),
                                0
                            ) period_credited,
                        
                            COALESCE(
                                SUM(amount_coins) FILTER (
                                    WHERE type = 'DEBIT'
                                    AND created_at >= ? AND created_at <= ?
                                ),
                                0
                            ) period_debited
                        
                        FROM app_user_wallet_transactions
                        """,
                fromTimestamp, toTimestamp,
                fromTimestamp, toTimestamp,
                fromTimestamp, toTimestamp,
                fromTimestamp, toTimestamp,
                fromTimestamp, toTimestamp
        );
    }

    // ---------------------------------------------------------
    // PURCHASE ANALYTICS
    // ---------------------------------------------------------

    public Map<String, Object> purchases() {
        return jdbc.queryForMap("""
                SELECT
                    COUNT(*) total,
                
                    COUNT(*) FILTER (
                        WHERE status = 'SUCCESS'
                    ) successful,
                
                    COUNT(*) FILTER (
                        WHERE status = 'PENDING'
                    ) pending,
                
                    COUNT(*) FILTER (
                        WHERE status = 'FAILED'
                    ) failed,
                
                    COUNT(*) FILTER (
                        WHERE status = 'CANCELLED'
                    ) cancelled,
                
                    COALESCE(
                        SUM(p.amount_coins + p.bonus_coins)
                        FILTER (WHERE x.status = 'SUCCESS'),
                        0
                    ) issued,
                
                    COALESCE(
                        SUM(p.amount_coins)
                        FILTER (WHERE x.status = 'SUCCESS'),
                        0
                    ) base_coins,
                
                    COALESCE(
                        SUM(p.bonus_coins)
                        FILTER (WHERE x.status = 'SUCCESS'),
                        0
                    ) bonus_coins
                
                FROM app_user_wallet_purchases x
                JOIN app_wallet_packages p ON p.id = x.package_id
                """);
    }

    public List<Map<String, Object>> purchaseCurrencies() {
        return jdbc.queryForList("""
                SELECT
                    currency,
                    COUNT(*) purchase_count,
                    COALESCE(SUM(amount_paid), 0) amount_paid
                FROM app_user_wallet_purchases
                WHERE status = 'SUCCESS'
                GROUP BY currency
                ORDER BY currency
                """);
    }

    public List<String> purchaseTrendCurrencies(Instant from, Instant to) {

        Timestamp fromTimestamp = timestamp(from);
        Timestamp toTimestamp = timestamp(to);

        return jdbc.queryForList("""
                        SELECT DISTINCT currency
                        FROM app_user_wallet_purchases
                        WHERE status = 'SUCCESS'
                          AND created_at >= ?
                          AND created_at <= ?
                        ORDER BY currency
                        """,
                String.class,
                fromTimestamp,
                toTimestamp
        );
    }

    // ---------------------------------------------------------
    // CASHOUT ANALYTICS
    // ---------------------------------------------------------

    public List<Map<String, Object>> cashoutStatuses() {
        return jdbc.queryForList("""
                SELECT
                    status,
                    COUNT(*) request_count,
                    COALESCE(SUM(amount_coins), 0) coins,
                    COALESCE(SUM(amount_rupees), 0) rupees
                FROM app_user_wallet_cashouts
                GROUP BY status
                ORDER BY status
                """);
    }

    public List<Map<String, Object>> cashoutTypes() {
        return jdbc.queryForList("""
                SELECT
                    type,
                    COUNT(*) request_count,
                    COALESCE(SUM(amount_coins), 0) coins,
                    COALESCE(SUM(amount_rupees), 0) rupees
                FROM app_user_wallet_cashouts
                GROUP BY type
                ORDER BY type
                """);
    }

    // ---------------------------------------------------------
    // PACKAGE PERFORMANCE ANALYTICS
    // ---------------------------------------------------------

    public List<Map<String, Object>> packagePerformance() {
        return jdbc.queryForList("""
                SELECT
                    p.id package_id,
                    p.name package_name,
                    p.active,
                    p.amount_coins,
                    p.bonus_coins,
                    p.price,
                    COALESCE(x.currency, p.currency) currency,
                
                    COUNT(x.id) successful_count,
                
                    COALESCE(SUM(p.amount_coins), 0) base_sold,
                    COALESCE(SUM(p.bonus_coins), 0) bonus_issued,
                    COALESCE(SUM(x.amount_paid), 0) amount_paid
                
                FROM app_wallet_packages p
                
                LEFT JOIN app_user_wallet_purchases x
                    ON x.package_id = p.id
                    AND x.status = 'SUCCESS'
                
                GROUP BY
                    p.id,
                    p.name,
                    p.active,
                    p.amount_coins,
                    p.bonus_coins,
                    p.price,
                    COALESCE(x.currency, p.currency),
                    p.display_order
                
                ORDER BY
                    p.display_order,
                    p.id,
                    COALESCE(x.currency, p.currency)
                """);
    }

    // ---------------------------------------------------------
    // TRANSACTION TREND ANALYTICS
    // ---------------------------------------------------------

    public List<Map<String, Object>> transactionTrend(
            String dateTrunc,
            Instant from,
            Instant to
    ) {

        String trunc = validatedDateTrunc(dateTrunc);
        Timestamp fromTimestamp = timestamp(from);
        Timestamp toTimestamp = timestamp(to);

        return jdbc.queryForList("""
                        SELECT
                            date_trunc(
                                '%s',
                                created_at AT TIME ZONE 'UTC'
                            )::date bucket,
                        
                            COUNT(*) transaction_count,
                        
                            COUNT(*) FILTER (
                                WHERE type = 'CREDIT'
                            ) credit_count,
                        
                            COUNT(*) FILTER (
                                WHERE type = 'DEBIT'
                            ) debit_count,
                        
                            COALESCE(
                                SUM(amount_coins) FILTER (WHERE type = 'CREDIT'),
                                0
                            ) credited,
                        
                            COALESCE(
                                SUM(amount_coins) FILTER (WHERE type = 'DEBIT'),
                                0
                            ) debited
                        
                        FROM app_user_wallet_transactions
                        
                        WHERE created_at >= ?
                          AND created_at <= ?
                        
                        GROUP BY bucket
                        ORDER BY bucket
                        """.formatted(trunc),
                fromTimestamp,
                toTimestamp
        );
    }

    // ---------------------------------------------------------
    // PURCHASE TREND ANALYTICS
    // ---------------------------------------------------------

    public List<Map<String, Object>> purchaseTrend(
            String dateTrunc,
            Instant from,
            Instant to
    ) {

        String trunc = validatedDateTrunc(dateTrunc);
        Timestamp fromTimestamp = timestamp(from);
        Timestamp toTimestamp = timestamp(to);

        return jdbc.queryForList("""
                        SELECT
                            date_trunc(
                                '%s',
                                x.created_at AT TIME ZONE 'UTC'
                            )::date bucket,
                            x.currency,
                            COUNT(*) purchase_count,
                            COALESCE(
                                SUM(x.amount_paid),
                                0
                            ) amount_paid,
                            COALESCE(
                                SUM(p.amount_coins),
                                0
                            ) base_coins,
                            COALESCE(
                                SUM(p.bonus_coins),
                                0
                            ) bonus_coins
                        
                        FROM app_user_wallet_purchases x
                        
                        JOIN app_wallet_packages p
                            ON p.id = x.package_id
                        
                        WHERE x.status = 'SUCCESS'
                          AND x.created_at >= ?
                          AND x.created_at <= ?
                        
                        GROUP BY bucket, x.currency
                        ORDER BY bucket, x.currency
                        """.formatted(trunc),
                fromTimestamp,
                toTimestamp
        );
    }

    // ---------------------------------------------------------
    // CASHOUT TREND ANALYTICS
    // ---------------------------------------------------------

    public List<Map<String, Object>> cashoutTrend(
            String dateTrunc,
            Instant from,
            Instant to
    ) {

        String trunc = validatedDateTrunc(dateTrunc);
        Timestamp fromTimestamp = timestamp(from);
        Timestamp toTimestamp = timestamp(to);

        return jdbc.queryForList("""
                        SELECT
                            date_trunc(
                                '%s',
                                created_at AT TIME ZONE 'UTC'
                            )::date bucket,
                        
                            COUNT(*) cashout_count,
                        
                            COUNT(*) FILTER (
                                WHERE status = 'PENDING'
                            ) pending_count,
                        
                            COUNT(*) FILTER (
                                WHERE status = 'SUCCESS'
                            ) successful_count,
                        
                            COUNT(*) FILTER (
                                WHERE status = 'FAILED'
                            ) failed_count,
                        
                            COALESCE(
                                SUM(amount_coins)
                                FILTER (WHERE status = 'SUCCESS'),
                                0
                            ) successful_coins,
                        
                            COALESCE(
                                SUM(amount_rupees)
                                FILTER (WHERE status = 'SUCCESS'),
                                0
                            ) successful_rupees
                        
                        FROM app_user_wallet_cashouts
                        
                        WHERE created_at >= ?
                          AND created_at <= ?
                        
                        GROUP BY bucket
                        ORDER BY bucket
                        """.formatted(trunc),
                fromTimestamp,
                toTimestamp
        );
    }

    // ---------------------------------------------------------
    // WALLET RULES
    // ---------------------------------------------------------

    public long totalRules() {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM app_wallet_rules",
                Long.class
        );
    }

    public long activeRules() {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM app_wallet_rules WHERE active = true",
                Long.class
        );
    }

    // ---------------------------------------------------------
    // WALLET PACKAGES
    // ---------------------------------------------------------

    public long totalPackages() {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM app_wallet_packages",
                Long.class
        );
    }

    public long activePackages() {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM app_wallet_packages WHERE active = true",
                Long.class
        );
    }

    // ---------------------------------------------------------
    // CASHOUT TOTAL
    // ---------------------------------------------------------

    public long totalCashouts() {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM app_user_wallet_cashouts",
                Long.class
        );
    }
}