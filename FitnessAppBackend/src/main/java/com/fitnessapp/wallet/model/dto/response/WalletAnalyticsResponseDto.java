package com.fitnessapp.wallet.model.dto.response;

import com.fitnessapp.wallet.model.WalletAnalyticsInterval;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record WalletAnalyticsResponseDto(
        Instant generatedAt, AnalyticsPeriod period, WalletBalanceAnalytics balance,
        WalletTransactionAnalytics transactions, WalletPurchaseAnalytics purchases,
        WalletCashoutAnalytics cashouts, WalletPackageAnalytics packages,
        WalletRulesAnalytics rules, WalletTrendAnalytics trends
) {
    public record AnalyticsPeriod(Instant from, Instant to, WalletAnalyticsInterval interval) {
    }

    public record WalletBalanceAnalytics(long totalWalletBalanceCoins, long averageBalanceCoins,
                                         long usersWithBalance, long usersWithZeroBalance, long totalUsers) {
    }

    public record WalletTransactionAnalytics(long totalTransactions, long totalCredits, long totalDebits,
                                             long totalCoinsCredited, long totalCoinsDebited, long periodTransactions,
                                             long periodCredits, long periodDebits, long periodCoinsCredited,
                                             long periodCoinsDebited) {
    }

    public record CurrencyBreakdown(String currency, long successfulPurchaseCount, BigDecimal totalAmountPaid) {
    }

    public record WalletPurchaseAnalytics(long totalPurchases, long successfulPurchases, long pendingPurchases,
                                          long failedPurchases, long cancelledPurchases, long totalBaseCoinsPurchased,
                                          long totalBonusCoinsIssued, long totalCoinsIssued,
                                          List<CurrencyBreakdown> currencyBreakdown) {
    }

    public record CashoutStatusBreakdown(String status, long count, long coins, BigDecimal amountRupees) {
    }

    public record CashoutTypeBreakdown(String type, long count, long coins, BigDecimal amountRupees) {
    }

    public record WalletCashoutAnalytics(long totalCashouts, long pendingCashouts, long successfulCashouts,
                                         long failedCashouts, long pendingCoins, long successfulCoins, long failedCoins,
                                         BigDecimal pendingAmountRupees, BigDecimal successfulAmountRupees,
                                         BigDecimal failedAmountRupees, List<CashoutStatusBreakdown> statusBreakdown,
                                         List<CashoutTypeBreakdown> typeBreakdown) {
    }

    public record PackagePerformance(long packageId, String packageName, boolean active, long baseCoins,
                                     long bonusCoins, double price, String currency, long successfulPurchaseCount,
                                     long totalCoinsSold, long totalBonusCoinsIssued, BigDecimal totalAmountPaid) {
    }

    public record WalletPackageAnalytics(long totalPackages, long activePackages, long inactivePackages,
                                         List<PackagePerformance> packagePerformance) {
    }

    public record WalletRulesAnalytics(long totalRules, long activeRules, long inactiveRules) {
    }

    public record TransactionTrend(LocalDate bucket, long transactionCount, long creditCount,
                                   long debitCount, long coinsCredited, long coinsDebited) {
    }

    public record PurchaseTrend(LocalDate bucket, String currency, long successfulPurchaseCount,
                                BigDecimal totalAmountPaid, long baseCoinsPurchased, long bonusCoinsIssued) {
    }

    public record CashoutTrend(LocalDate bucket, long cashoutCount, long pendingCount, long successfulCount,
                               long failedCount, long successfulCoins, BigDecimal successfulAmountRupees) {
    }

    public record WalletTrendAnalytics(List<TransactionTrend> transactionTrend,
                                       List<PurchaseTrend> purchaseTrend, List<CashoutTrend> cashoutTrend) {
    }
}
