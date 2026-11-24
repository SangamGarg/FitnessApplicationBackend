package com.fitnessapp.wallet.service;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.model.enums.MobileAccountStatus;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.portal.audit.service.PortalAuditService;
import com.fitnessapp.wallet.model.WalletAnalyticsInterval;
import com.fitnessapp.wallet.model.WalletTransactionType;
import com.fitnessapp.wallet.model.dto.request.*;
import com.fitnessapp.wallet.model.dto.response.*;
import com.fitnessapp.wallet.model.entity.WalletAccountEntity;
import com.fitnessapp.wallet.model.entity.WalletPackageEntity;
import com.fitnessapp.wallet.model.entity.WalletRulesEntity;
import com.fitnessapp.wallet.model.entity.WalletTransactionEntity;
import com.fitnessapp.wallet.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortalServiceImpl implements PortalService {

    private final UserRepo userRepo;
    private final WalletAccountRepository wallets;
    private final WalletTransactionRepository transactions;
    private final WalletRulesRepository rulesRepo;
    private final WalletPackageRepository packageRepo;
    private final WalletCashoutRepository cashoutRepo;
    private final WalletPurchaseRepository purchaseRepo;
    private final WalletAnalyticsRepository analyticsRepo;
    private final PortalAuditService audit;

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public WalletAnalyticsResponseDto getWalletAnalytics(Instant from, Instant to, WalletAnalyticsInterval interval) {
        Instant now = Instant.now();
        Instant trendTo = to == null ? now : to;
        Instant trendFrom = from == null ? (to == null ? now.minus(30, ChronoUnit.DAYS) : trendTo.minus(30, ChronoUnit.DAYS)) : from;
        if (trendFrom.isAfter(trendTo))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must not be after to");
        if (interval == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "interval is required");
        String trunc = switch (interval) {
            case DAILY -> "day";
            case WEEKLY -> "week";
            case MONTHLY -> "month";
        };
        log.info("Generating wallet analytics from {} to {} using {} intervals", trendFrom, trendTo, interval);

        Map<String, Object> balance = analyticsRepo.balance(), tx = analyticsRepo.transactions(trendFrom, trendTo), purchases = analyticsRepo.purchases();
        long users = userRepo.count(), positive = number(balance, "positive_wallets"), totalBalance = number(balance, "total_balance");
        var currencies = analyticsRepo.purchaseCurrencies().stream().map(row -> new WalletAnalyticsResponseDto.CurrencyBreakdown(
                string(row, "currency"), number(row, "purchase_count"), decimal(row, "amount_paid"))).toList();
        var statuses = analyticsRepo.cashoutStatuses().stream().map(row -> new WalletAnalyticsResponseDto.CashoutStatusBreakdown(
                string(row, "status"), number(row, "request_count"), number(row, "coins"), decimal(row, "rupees"))).toList();
        var types = analyticsRepo.cashoutTypes().stream().map(row -> new WalletAnalyticsResponseDto.CashoutTypeBreakdown(
                string(row, "type"), number(row, "request_count"), number(row, "coins"), decimal(row, "rupees"))).toList();
        Map<String, WalletAnalyticsResponseDto.CashoutStatusBreakdown> statusMap = new HashMap<>();
        statuses.forEach(s -> statusMap.put(s.status(), s));
        var pending = statusMap.getOrDefault("PENDING", new WalletAnalyticsResponseDto.CashoutStatusBreakdown("PENDING", 0, 0, BigDecimal.ZERO));
        var success = statusMap.getOrDefault("SUCCESS", new WalletAnalyticsResponseDto.CashoutStatusBreakdown("SUCCESS", 0, 0, BigDecimal.ZERO));
        var failed = statusMap.getOrDefault("FAILED", new WalletAnalyticsResponseDto.CashoutStatusBreakdown("FAILED", 0, 0, BigDecimal.ZERO));
        var packagePerformance = analyticsRepo.packagePerformance().stream().map(row -> new WalletAnalyticsResponseDto.PackagePerformance(
                number(row, "package_id"), string(row, "package_name"), bool(row, "active"), number(row, "amount_coins"), number(row, "bonus_coins"),
                decimal(row, "price").doubleValue(), string(row, "currency"), number(row, "successful_count"),
                number(row, "base_sold") + number(row, "bonus_issued"), number(row, "bonus_issued"), decimal(row, "amount_paid"))).toList();

        return new WalletAnalyticsResponseDto(Instant.now(), new WalletAnalyticsResponseDto.AnalyticsPeriod(trendFrom, trendTo, interval),
                new WalletAnalyticsResponseDto.WalletBalanceAnalytics(totalBalance, users == 0 ? 0 : totalBalance / users, positive, Math.max(0, users - positive), users),
                new WalletAnalyticsResponseDto.WalletTransactionAnalytics(number(tx, "total"), number(tx, "credits"), number(tx, "debits"), number(tx, "credited"), number(tx, "debited"), number(tx, "period_total"), number(tx, "period_credits"), number(tx, "period_debits"), number(tx, "period_credited"), number(tx, "period_debited")),
                new WalletAnalyticsResponseDto.WalletPurchaseAnalytics(number(purchases, "total"), number(purchases, "successful"), number(purchases, "pending"), number(purchases, "failed"), number(purchases, "cancelled"), number(purchases, "base_coins"), number(purchases, "bonus_coins"), number(purchases, "issued"), currencies),
                new WalletAnalyticsResponseDto.WalletCashoutAnalytics(analyticsRepo.totalCashouts(), pending.count(), success.count(), failed.count(), pending.coins(), success.coins(), failed.coins(), pending.amountRupees(), success.amountRupees(), failed.amountRupees(), statuses, types),
                new WalletAnalyticsResponseDto.WalletPackageAnalytics(analyticsRepo.totalPackages(), analyticsRepo.activePackages(), analyticsRepo.totalPackages() - analyticsRepo.activePackages(), packagePerformance),
                new WalletAnalyticsResponseDto.WalletRulesAnalytics(analyticsRepo.totalRules(), analyticsRepo.activeRules(), analyticsRepo.totalRules() - analyticsRepo.activeRules()),
                new WalletAnalyticsResponseDto.WalletTrendAnalytics(transactionTrends(trendFrom, trendTo, trunc), purchaseTrends(trendFrom, trendTo, trunc), cashoutTrends(trendFrom, trendTo, trunc)));
    }

    private List<WalletAnalyticsResponseDto.TransactionTrend> transactionTrends(Instant from, Instant to, String trunc) {
        Map<LocalDate, Map<String, Object>> rows = rowsByDate(analyticsRepo.transactionTrend(trunc, from, to));
        List<WalletAnalyticsResponseDto.TransactionTrend> out = new ArrayList<>();
        for (LocalDate d : trendBuckets(from, to, trunc)) {
            Map<String, Object> r = rows.get(d);
            out.add(new WalletAnalyticsResponseDto.TransactionTrend(d, number(r, "transaction_count"), number(r, "credit_count"), number(r, "debit_count"), number(r, "credited"), number(r, "debited")));
        }
        return out;
    }

    private List<WalletAnalyticsResponseDto.CashoutTrend> cashoutTrends(Instant from, Instant to, String trunc) {
        Map<LocalDate, Map<String, Object>> rows = rowsByDate(analyticsRepo.cashoutTrend(trunc, from, to));
        List<WalletAnalyticsResponseDto.CashoutTrend> out = new ArrayList<>();
        for (LocalDate d : trendBuckets(from, to, trunc)) {
            Map<String, Object> r = rows.get(d);
            out.add(new WalletAnalyticsResponseDto.CashoutTrend(d, number(r, "cashout_count"), number(r, "pending_count"), number(r, "successful_count"), number(r, "failed_count"), number(r, "successful_coins"), decimal(r, "successful_rupees")));
        }
        return out;
    }

    private List<WalletAnalyticsResponseDto.PurchaseTrend> purchaseTrends(Instant from, Instant to, String trunc) {
        Map<String, Map<String, Object>> rows = new HashMap<>();
        for (Map<String, Object> row : analyticsRepo.purchaseTrend(trunc, from, to))
            rows.put(date(row, "bucket") + "|" + string(row, "currency"), row);
        List<WalletAnalyticsResponseDto.PurchaseTrend> out = new ArrayList<>();
        List<String> currencies = analyticsRepo.purchaseTrendCurrencies(from, to);
        for (LocalDate bucket : trendBuckets(from, to, trunc))
            for (String currency : currencies) {
                Map<String, Object> row = rows.get(bucket + "|" + currency);
                out.add(new WalletAnalyticsResponseDto.PurchaseTrend(bucket, currency, number(row, "purchase_count"), decimal(row, "amount_paid"), number(row, "base_coins"), number(row, "bonus_coins")));
            }
        return out;
    }

    private Map<LocalDate, Map<String, Object>> rowsByDate(List<Map<String, Object>> rows) {
        Map<LocalDate, Map<String, Object>> map = new HashMap<>();
        rows.forEach(r -> map.put(date(r, "bucket"), r));
        return map;
    }

    private List<LocalDate> trendBuckets(Instant from, Instant to, String trunc) {
        LocalDate start = LocalDate.ofInstant(from, ZoneOffset.UTC), end = LocalDate.ofInstant(to, ZoneOffset.UTC);
        ChronoUnit step = ChronoUnit.DAYS;
        if ("week".equals(trunc)) {
            start = start.with(java.time.DayOfWeek.MONDAY);
            step = ChronoUnit.WEEKS;
        } else if ("month".equals(trunc)) {
            start = start.withDayOfMonth(1);
            step = ChronoUnit.MONTHS;
        }
        List<LocalDate> result = new ArrayList<>();
        while (!start.isAfter(end)) {
            result.add(start);
            start = start.plus(1, step);
        }
        return result;
    }

    private long number(Map<String, Object> row, String key) {
        return row == null ? 0 : ((Number) row.get(key)).longValue();
    }

    private String string(Map<String, Object> row, String key) {
        return row == null || row.get(key) == null ? null : row.get(key).toString();
    }

    private BigDecimal decimal(Map<String, Object> row, String key) {
        return row == null || row.get(key) == null ? BigDecimal.ZERO : new BigDecimal(row.get(key).toString());
    }

    private boolean bool(Map<String, Object> row, String key) {
        return row != null && Boolean.parseBoolean(row.get(key).toString());
    }

    private LocalDate date(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v instanceof java.sql.Date d ? d.toLocalDate() : LocalDate.parse(v.toString());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PortalWalletCashoutResponseDto> getCashouts(WalletCashoutFilterRequestDto filters, Pageable pageable) {
        if (filters.getCreatedFrom() != null && filters.getCreatedTo() != null
                && filters.getCreatedFrom().isAfter(filters.getCreatedTo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "createdFrom must not be after createdTo");
        }
        String transactionId = filters.getTransactionId() == null || filters.getTransactionId().isBlank()
                ? "" : filters.getTransactionId().trim();
        String userId = filters.getEmailId() == null || filters.getEmailId().isBlank()
                ? "" : getUserIdFromEmail(filters.getEmailId());

        return cashoutRepo.searchCashouts(userId, filters.getStatus(), filters.getType(), transactionId,
                filters.getCreatedFrom(), filters.getCreatedTo(), pageable).map(PortalWalletCashoutResponseDto::from);
    }

    @Override
    @Transactional
    public PortalWalletCashoutResponseDto updateCashoutStatus(Long id, WalletCashoutStatusUpdateRequestDto request) {
        var cashout = cashoutRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet cashout not found"));
        cashout.setStatus(request.status());
        if (request.transactionId() != null)
            cashout.setTransactionId(request.transactionId().isBlank() ? null : request.transactionId().trim());
        if (request.remarks() != null)
            cashout.setRemarks(request.remarks().isBlank() ? null : request.remarks().trim());
        cashout = cashoutRepo.save(cashout);
        audit.record("WALLET_CASHOUT_STATUS", "WALLET", cashout.getUser().getId(), "Changed cashout status: " + id + " to " + request.status());
        return PortalWalletCashoutResponseDto.from(cashout);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PortalWalletPurchaseResponseDto> getUserPurchases(String emailId, Pageable pageable) {
        String userId = getUserIdFromEmail(emailId);
        if (userId == null || userId.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        return purchaseRepo.findAllByUserIdOrderByCreatedAtDesc(userId.trim(), pageable).map(PortalWalletPurchaseResponseDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletPackageResponseDto> getWalletPackages() {
        return packageRepo.findAll().stream().map(WalletPackageResponseDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WalletPackageResponseDto getWalletPackage(Long id) {
        return WalletPackageResponseDto.from(findWalletPackage(id));
    }

    @Override
    @Transactional
    public WalletPackageResponseDto createWalletPackage(WalletPackageRequestDto request) {
        validatePackage(request, true);
        WalletPackageEntity entity = new WalletPackageEntity();
        applyPackage(entity, request, false);
        entity = packageRepo.save(entity);
        audit.record("WALLET_PACKAGE_CREATE", "WALLET", "SYSTEM", "Created package: " + entity.getName());
        return WalletPackageResponseDto.from(entity);
    }

    @Override
    @Transactional
    public WalletPackageResponseDto updateWalletPackage(Long id, WalletPackageRequestDto request) {
        validatePackage(request, true);
        WalletPackageEntity entity = findWalletPackage(id);
        applyPackage(entity, request, false);
        entity = packageRepo.save(entity);
        audit.record("WALLET_PACKAGE_UPDATE", "WALLET", "SYSTEM", "Updated package: " + id);
        return WalletPackageResponseDto.from(entity);
    }

    @Override
    @Transactional
    public WalletPackageResponseDto patchWalletPackage(Long id, WalletPackageRequestDto request) {
        validatePackage(request, false);
        WalletPackageEntity entity = findWalletPackage(id);
        applyPackage(entity, request, true);
        entity = packageRepo.save(entity);
        audit.record("WALLET_PACKAGE_UPDATE", "WALLET", "SYSTEM", "Patched package: " + id);
        return WalletPackageResponseDto.from(entity);
    }

    @Override
    @Transactional
    public WalletPackageResponseDto updateWalletPackageStatus(Long id, boolean active) {
        WalletPackageEntity entity = findWalletPackage(id);
        entity.setActive(active);
        entity = packageRepo.save(entity);
        audit.record("WALLET_PACKAGE_STATUS", "WALLET", "SYSTEM", "Changed package status: " + id);
        return WalletPackageResponseDto.from(entity);
    }

    @Override
    @Transactional
    public void deleteWalletPackage(Long id) {
        WalletPackageEntity entity = findWalletPackage(id);
        packageRepo.delete(entity);
        audit.record("WALLET_PACKAGE_DELETE", "WALLET", "SYSTEM", "Deleted package: " + id);
    }

    private WalletPackageEntity findWalletPackage(Long id) {
        return packageRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet package not found"));
    }

    private void validatePackage(WalletPackageRequestDto request, boolean complete) {
        if ((complete && (request.getName() == null || request.getName().isBlank() || request.getAmountCoins() == null
                || request.getBonusCoins() == null || request.getPrice() == null || request.getCurrency() == null || request.getCurrency().isBlank()))
                || request.getName() != null && request.getName().isBlank()
                || request.getAmountCoins() != null && request.getAmountCoins() < 0
                || request.getBonusCoins() != null && request.getBonusCoins() < 0
                || request.getPrice() != null && request.getPrice() < 0
                || request.getCurrency() != null && request.getCurrency().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wallet package details are invalid");
        }
    }

    private void applyPackage(WalletPackageEntity entity, WalletPackageRequestDto request, boolean partial) {
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getAmountCoins() != null) entity.setAmountCoins(request.getAmountCoins());
        if (request.getBonusCoins() != null) entity.setBonusCoins(request.getBonusCoins());
        if (request.getPrice() != null) entity.setPrice(request.getPrice());
        if (request.getCurrency() != null) entity.setCurrency(request.getCurrency());
        if (request.getTag() != null || !partial) entity.setTag(request.getTag());
        if (request.getDescription() != null || !partial) entity.setDescription(request.getDescription());
        if (request.getActive() != null) entity.setActive(request.getActive());
        if (request.getDisplayOrder() != null) entity.setDisplayOrder(request.getDisplayOrder());
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppUserForWalletResponseDto> getAppUsers() {
        List<UserEntity> list = userRepo.findAll();
        return list.stream().map(AppUserForWalletResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<WalletRulesEntity> getAllWalletRules() {
        return rulesRepo.findAll();
    }

    @Transactional
    @Override
    public WalletRulesEntity saveWalletRule(WalletRuleRequestDto request) {
        WalletRulesEntity rule = WalletRulesEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .active(request.isActive())
                .build();
        rule = rulesRepo.save(rule);
        audit.record("WALLET_RULE_CREATE", "WALLET", "SYSTEM", "Created rule: " + rule.getTitle());
        return rule;
    }

    @Transactional
    @Override
    public WalletRulesEntity updateWalletRule(Long id, WalletRuleRequestDto request) {
        WalletRulesEntity rule = rulesRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet rule not found"));

        rule.setTitle(request.getTitle());
        rule.setDescription(request.getDescription());
        rule.setDisplayOrder(request.getDisplayOrder());
        rule.setActive(request.isActive());

        rule = rulesRepo.save(rule);
        audit.record("WALLET_RULE_UPDATE", "WALLET", "SYSTEM", "Updated rule: " + id);
        return rule;
    }

    @Transactional
    @Override
    public void deleteWalletRule(Long id) {
        if (!rulesRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet rule not found");
        }
        rulesRepo.deleteById(id);
        audit.record("WALLET_RULE_DELETE", "WALLET", "SYSTEM", "Deleted rule: " + id);
    }

    @Transactional(readOnly = true)
    @Override
    public WalletBalanceResponseDto getBalance(String emailId) {
        String userId = getUserIdFromEmail(emailId);
        return wallets.findByUserId(userId)
                .map(wallet -> new WalletBalanceResponseDto(userId, wallet.getBalanceCoins(), wallet.getUpdatedAt()))
                .orElseGet(() -> new WalletBalanceResponseDto(userId, 0, null));
    }


    @Transactional(readOnly = true)
    @Override
    public Page<WalletTransactionResponseDto> getTransactions(String emailId, Pageable pageable) {
        String userId = getUserIdFromEmail(emailId);
        return transactions.findAllByWalletUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
    }

    @Transactional
    @Override
    public WalletTransactionResponseDto adjustAsAdmin(String emailId, WalletAdjustmentRequestDto request) {
        String userId = getUserIdFromEmail(emailId);
        String actorEmail = getCurrentUserPortal();
        WalletTransactionEntity transaction = apply(
                userId,
                request.type(),
                request.amountCoins(),
                request.reason(),
                request.reference(),
                request.idempotencyKey(),
                actorEmail,
                false
        );
        audit.record(
                "WALLET_" + request.type().name(),
                "WALLET",
                userId,
                request.reason() + "; transaction=" + transaction.getId()
        );
        return toResponse(transaction);
    }

    @Transactional
    public WalletTransactionResponseDto debitForBusiness(
            String userId,
            long amountCoins,
            String reason,
            String reference,
            String idempotencyKey
    ) {
        return toResponse(apply(
                userId,
                WalletTransactionType.DEBIT,
                amountCoins,
                reason,
                reference,
                idempotencyKey,
                "system",
                true
        ));
    }

    @Transactional
    public WalletTransactionResponseDto creditForBusiness(
            String userId,
            long amountCoins,
            String reason,
            String reference,
            String idempotencyKey
    ) {
        return toResponse(apply(
                userId,
                WalletTransactionType.CREDIT,
                amountCoins,
                reason,
                reference,
                idempotencyKey,
                "system",
                false
        ));
    }

    private WalletTransactionEntity apply(
            String userId,
            WalletTransactionType type,
            long amountCoins,
            String reason,
            String reference,
            String idempotencyKey,
            String actorEmail,
            boolean requireActive
    ) {
        if (amountCoins <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coin amount must be positive");
        }
        String normalizedReason = reason == null ? null : reason.trim();
        String normalizedIdempotencyKey = idempotencyKey == null ? null : idempotencyKey.trim();
        String normalizedReference = normalize(reference);
        if (normalizedReason == null || normalizedReason.isBlank() || normalizedReason.length() > 300
                || normalizedIdempotencyKey == null || normalizedIdempotencyKey.isBlank()
                || normalizedIdempotencyKey.length() > 100
                || (reference != null && reference.length() > 160)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wallet transaction details are invalid");
        }

        UserEntity user = userRepo.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mobile user not found"));
        if (requireActive && user.getAccountStatus() != MobileAccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Inactive mobile users cannot transact");
        }

        WalletAccountEntity wallet = wallets.findByUserIdForUpdate(userId)
                .orElseGet(() -> wallets.saveAndFlush(new WalletAccountEntity(user)));
        var existing = transactions.findByWalletUserIdAndIdempotencyKey(userId, normalizedIdempotencyKey);
        if (existing.isPresent()) {
            WalletTransactionEntity previous = existing.get();
            if (previous.getType() != type
                    || previous.getAmountCoins() != amountCoins
                    || !previous.getReason().equals(normalizedReason)
                    || !java.util.Objects.equals(previous.getReference(), normalizedReference)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Idempotency key was already used for a different wallet operation"
                );
            }
            return previous;
        }

        long nextBalance;
        try {
            nextBalance = type == WalletTransactionType.CREDIT
                    ? Math.addExact(wallet.getBalanceCoins(), amountCoins)
                    : Math.subtractExact(wallet.getBalanceCoins(), amountCoins);
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Wallet balance is outside the supported range");
        }
        if (nextBalance < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient coin balance");
        }

        wallet.setBalanceCoins(nextBalance);
        WalletTransactionEntity transaction = new WalletTransactionEntity(
                wallet,
                type,
                amountCoins,
                nextBalance,
                normalizedReason,
                normalizedReference,
                normalizedIdempotencyKey,
                actorEmail == null || actorEmail.isBlank() ? "system" : actorEmail
        );
        return transactions.saveAndFlush(transaction);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private WalletTransactionResponseDto toResponse(WalletTransactionEntity transaction) {
        return new WalletTransactionResponseDto(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmountCoins(),
                transaction.getBalanceAfter(),
                transaction.getReason(),
                transaction.getReference(),
                transaction.getIdempotencyKey(),
                transaction.getCreatedAt()
        );
    }

    public String getCurrentUserPortal() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {

            throw new IllegalStateException(
                    "No authenticated user found"
            );
        }

        return authentication.getName();
    }

    private String getUserIdFromEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ID is required");
        }
        UserEntity user = userRepo.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "App user not found"));
        return user.getId();
    }
}
