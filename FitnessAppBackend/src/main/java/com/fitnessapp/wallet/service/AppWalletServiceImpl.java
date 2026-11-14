package com.fitnessapp.wallet.service;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.model.enums.MobileAccountStatus;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.wallet.model.PurchaseStatus;
import com.fitnessapp.wallet.model.WalletCashoutStatus;
import com.fitnessapp.wallet.model.WalletTransactionType;
import com.fitnessapp.wallet.model.dto.request.WalletCashoutRequestDto;
import com.fitnessapp.wallet.model.dto.response.*;
import com.fitnessapp.wallet.model.entity.*;
import com.fitnessapp.wallet.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppWalletServiceImpl implements AppWalletService {

    private final UserRepo users;
    private final WalletAccountRepository wallets;
    private final WalletTransactionRepository transactions;
    private final WalletRulesRepository rulesRepo;
    private final WalletReferralRepository referralRepo;
    private final WalletReferralHistoryRepository referralHistoryRepo;
    private final WalletPackageRepository packageRepo;
    private final WalletPurchaseRepository purchaseRepo;
    private final WalletCashoutRepository cashoutRepo;

    @Value("${wallet.cashout.threshold-coins:1000}")
    private double thresholdCoins;

    @Value("${wallet.cashout.rupee-conversion-rate:100}")
    private double rupeeConversionRate;

    @Value("${wallet.referral.reward-coins:150}")
    private long referralRewardCoins;

    @Value("${wallet.cashout.max-coins-per-transaction:50000}")
    private long maxCoinsPerTransaction;

    @Value("${wallet.cashout.max-coins-per-day:100000}")
    private long maxCoinsPerDay;


    @Override
    @Transactional(readOnly = true)
    public WalletHomeResponseDto getWalletHome() {
        String userId = currentUserId();
        long balance = wallets.findByUserId(userId)
                .map(WalletAccountEntity::getBalanceCoins)
                .orElse(0L);

        double progress = Math.min(1.0, (double) balance / thresholdCoins);
        double rupeeValue = balance / rupeeConversionRate;

        WalletHomeResponseDto.BalanceBoxDto balanceBox = WalletHomeResponseDto.BalanceBoxDto.builder()
                .title("Current MP Coins Balance")
                .threshouldReqString(String.format("Min %.0f required for cashout", thresholdCoins))
                .eligibleString("Eligible for cashout")
                .currentBalance(balance)
                .thresholdProgress(progress)
                .currencyValue(rupeeValue)
                .build();

        return WalletHomeResponseDto.builder()
                .title("Fitness Wallet")
                .subtitle("Manage your earnings")
                .description("Track your fitness activities and earn MP coins. You can redeem these coins for real money once you reach the threshold.")
                .balanceBox(balanceBox)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WalletRulesResponseDto getWalletRules() {
        var rules = rulesRepo.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(rule -> WalletRulesResponseDto.WalletRuleDto.builder()
                        .title(rule.getTitle())
                        .description(rule.getDescription())
                        .build())
                .toList();

        return WalletRulesResponseDto.builder()
                .header("Wallet Rules")
                .subheader("Terms and conditions for using the fitness wallet")
                .rules(rules)
                .build();
    }

    @Override
    @Transactional
    public WalletEarnResponseDto getWalletEarn() {
        String userId = currentUserId();
        UserEntity user = findUser(userId);

        WalletReferralEntity referral = referralRepo.findByUserId(userId)
                .orElseGet(() -> {
                    String name = "User";
                    if (user.getUserDetailsEntity() != null && user.getUserDetailsEntity().getName() != null) {
                        name = user.getUserDetailsEntity().getName();
                    }

                    String prefix = name.length() >= 2 ? name.substring(0, 2).toUpperCase() : name.toUpperCase();
                    if (prefix.length() < 2) prefix = (prefix + "X").substring(0, 2).toUpperCase();

                    String referralCode;
                    do {
                        referralCode = prefix + "-MP-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                    } while (referralRepo.existsByReferralCode(referralCode));

                    WalletReferralEntity newReferral = WalletReferralEntity.builder()
                            .user(user)
                            .referralCode(referralCode)
                            .totalEarnedMpCoins(0)
                            .totalFriendsInvited(0)
                            .build();
                    return referralRepo.save(newReferral);
                });

        List<WalletReferralHistoryEntity> history = referralHistoryRepo.findAllByReferrerIdOrderByCreatedAtDesc(userId);

        List<WalletEarnResponseDto.ReferralFriendDto> referredFriends = history.stream()
                .map(h -> WalletEarnResponseDto.ReferralFriendDto.builder()
                        .name(h.getReferredUser().getUserDetailsEntity() != null ? h.getReferredUser().getUserDetailsEntity().getName() : "Anonymous")
                        .status(h.getStatus())
                        .earnedCoins(h.getEarnedCoins())
                        .build())
                .toList();

        WalletEarnResponseDto.ReferralBoxDto referralBox = WalletEarnResponseDto.ReferralBoxDto.builder()
                .header("Your unique referral code")
                .referralCode(referral.getReferralCode())
                .totalEarnedMpCoins(referral.getTotalEarnedMpCoins())
                .friendInvitedText("Friends Invited")
                .totalFriendsInvited(referral.getTotalFriendsInvited())
                .build();

        return WalletEarnResponseDto.builder()
                .title(String.format("Invite friend, earn %.0f MP each", (double) referralRewardCoins))
                .subtitle("Refer to your friend and get MP coins each after first challenge completed by friend")
                .description("Share your referral code with friends and earn rewards when they join and complete their first challenge.")
                .referralBox(referralBox)
                .referredFriends(referredFriends)
                .build();
    }


    @Override
    @Transactional
    public void linkReferral(String newUserId, String referralCode) {
        log.info("Linking referral code {} for new user {}", referralCode, newUserId);

        referralRepo.findByReferralCode(referralCode).ifPresent(referrerReferral -> {
            UserEntity referrer = referrerReferral.getUser();
            UserEntity referredUser = findUser(newUserId);

            if (referrer.getId().equals(newUserId)) {
                log.warn("User {} tried to refer themselves", newUserId);
                return;
            }

            // Check if already referred
            if (referralHistoryRepo.findByReferredUserId(newUserId).isPresent()) {
                log.warn("User {} is already linked to a referrer", newUserId);
                return;
            }

            WalletReferralHistoryEntity history = WalletReferralHistoryEntity.builder()
                    .referrer(referrer)
                    .referredUser(referredUser)
                    .status("PENDING")
                    .earnedCoins(0)
                    .build();
            referralHistoryRepo.save(history);
            log.info("Successfully linked user {} to referrer {}", newUserId, referrer.getId());
        });
    }

    @Override
    @Transactional
    public void processReferralReward(String referredUserId) {
        referralHistoryRepo.findByReferredUserId(referredUserId).ifPresent(history -> {
            if ("PENDING".equals(history.getStatus())) {
                log.info("Processing referral reward for referred user {}", referredUserId);

                String referrerId = history.getReferrer().getId();

                // Credit Referrer
                creditForBusiness(
                        referrerId,
                        referralRewardCoins,
                        "Referral reward: Invited friend " + referredUserId,
                        "REF-INV-" + referredUserId,
                        UUID.randomUUID().toString()
                );

                // Credit Referred User
                creditForBusiness(
                        referredUserId,
                        referralRewardCoins,
                        "Referral reward: Joined via " + referrerId,
                        "REF-JOIN-" + referrerId,
                        UUID.randomUUID().toString()
                );

                // Update history
                history.setStatus("COMPLETED");
                history.setEarnedCoins(referralRewardCoins);
                referralHistoryRepo.save(history);

                // Update referrer stats
                referralRepo.findByUserId(referrerId).ifPresent(r -> {
                    r.setTotalFriendsInvited(r.getTotalFriendsInvited() + 1);
                    r.setTotalEarnedMpCoins(r.getTotalEarnedMpCoins() + referralRewardCoins);
                    referralRepo.save(r);
                });
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public WalletBuyResponseDto getWalletBuyInfo() {
        var packages = packageRepo.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(pkg -> WalletBuyResponseDto.WalletPackageDto.builder()
                        .id(pkg.getId())
                        .name(pkg.getName())
                        .amountCoins(pkg.getAmountCoins())
                        .bonusCoins(pkg.getBonusCoins())
                        .price(pkg.getPrice())
                        .currency(pkg.getCurrency())
                        .tag(pkg.getTag())
                        .description(pkg.getDescription())
                        .build())
                .toList();

        return WalletBuyResponseDto.builder()
                .title("Buy MP Coins")
                .subtitle("Get more coins to unlock premium features")
                .description("Choose a package that best suits your needs. Extra bonus coins on higher packages!")
                .packages(packages)
                .build();
    }

    @Override
    @Transactional
    public void initiatePurchase(Long packageId, String transactionId) {
        String userId = currentUserId();
        UserEntity user = findUser(userId);

        WalletPackageEntity pkg = packageRepo.findById(packageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found"));

        if (!pkg.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Package is not active");
        }

        var existing = purchaseRepo.findByTransactionId(transactionId);
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transaction ID already exists");
        }

        WalletPurchaseEntity purchase = new WalletPurchaseEntity();
        purchase.setUser(user);
        purchase.setWalletPackage(pkg);
        purchase.setAmountPaid(pkg.getPrice());
        purchase.setCurrency(pkg.getCurrency());
        purchase.setTransactionId(transactionId);
        purchase.setStatus(PurchaseStatus.PENDING);

        purchaseRepo.save(purchase);
    }

    @Override
    @Transactional
    public void completePurchase(String transactionId, boolean success) {
        WalletPurchaseEntity purchase = purchaseRepo.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase record not found"));

        if (purchase.getStatus() != PurchaseStatus.PENDING) {
            log.info("Purchase {} already processed with status {}", transactionId, purchase.getStatus());
            return;
        }

        if (success) {
            purchase.setStatus(PurchaseStatus.SUCCESS);
            long totalCoins = purchase.getWalletPackage().getAmountCoins() + purchase.getWalletPackage().getBonusCoins();

            WalletTransactionResponseDto walletTransactionResponseDto = creditForBusiness(
                    purchase.getUser().getId(),
                    totalCoins,
                    "Purchase of package: " + purchase.getWalletPackage().getName(),
                    "PURCHASE_" + transactionId,
                    "IDEM_PURCHASE_" + transactionId
            );
        } else {
            purchase.setStatus(PurchaseStatus.FAILED);
        }

        purchase.setUpdatedAt(java.time.Instant.now());
        purchaseRepo.save(purchase);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletBalanceResponseDto getBalance() {
        String userId = currentUserId();
        UserEntity user = findUser(userId);
        return wallets.findByUserId(userId)
                .map(wallet -> new WalletBalanceResponseDto(user.getId(), wallet.getBalanceCoins(), wallet.getUpdatedAt()))
                .orElseGet(() -> new WalletBalanceResponseDto(user.getId(), 0, null));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<WalletTransactionResponseDto> getTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String userId = currentUserId();
        findUser(userId);
        return transactions.findAllByWalletUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletCashoutResponseDto getWalletCashoutInfo() {
        List<WalletCashoutResponseDto.CashoutMethodDto> methods = List.of(
                new WalletCashoutResponseDto.CashoutMethodDto("UPI", "UPI Transfer", "upi_icon", "Enter UPI ID (e.g. user@upi)"),
                new WalletCashoutResponseDto.CashoutMethodDto("BANK", "Bank Transfer", "bank_icon", "Enter Account No, IFSC, Holder Name")
        );

        return WalletCashoutResponseDto.builder()
                .title("Cashout MP Coins")
                .subtitle("Convert your hard-earned coins to real money")
                .description("You can request a cashout once you reach the minimum threshold. Payments are usually processed within 24-48 hours.")
                .minCoinsRequired((long) thresholdCoins)
                .conversionRate(rupeeConversionRate)
                .methods(methods)
                .build();
    }

    @Override
    @Transactional
    public void requestCashout(WalletCashoutRequestDto request) {
        String userId = currentUserId();
        UserEntity user = findUser(userId);

        if (request.getAmountCoins() < thresholdCoins) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum cashout amount is " + thresholdCoins + " coins");
        }

        if (request.getAmountCoins() > maxCoinsPerTransaction) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum cashout per transaction is " + maxCoinsPerTransaction + " coins");
        }

        // Daily limit check
        Instant since = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Long coinsToday = cashoutRepo.sumAmountCoinsByUserIdAndCreatedAtAfterAndStatusNot(userId, since, WalletCashoutStatus.FAILED);
        if (coinsToday == null) coinsToday = 0L;

        if (coinsToday + request.getAmountCoins() > maxCoinsPerDay) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Daily cashout limit of " + maxCoinsPerDay + " coins exceeded");
        }

        // Rate limiting: Only one PENDING cashout allowed at a time
        if (cashoutRepo.existsByUserIdAndStatus(userId, WalletCashoutStatus.PENDING)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "You already have a pending cashout request. Please wait for it to be processed.");
        }

        // Check balance
        WalletAccountEntity wallet = wallets.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        if (wallet.getBalanceCoins() < request.getAmountCoins()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance for cashout");
        }

        // Idempotency check
        if (request.getTransactionId() != null && cashoutRepo.findByTransactionId(request.getTransactionId()).isPresent()) {
            log.info("Cashout request with transactionId {} already exists", request.getTransactionId());
            return;
        }

        double rupees = request.getAmountCoins() / rupeeConversionRate;

        // Record cashout request
        WalletCashoutEntity cashout = new WalletCashoutEntity();
        cashout.setUser(user);
        cashout.setAmountCoins(request.getAmountCoins());
        cashout.setAmountRupees(rupees);
        cashout.setType(request.getType());
        cashout.setDetails(request.getDetails());
        cashout.setStatus(WalletCashoutStatus.PENDING);
        cashout.setTransactionId(request.getTransactionId());
        cashoutRepo.save(cashout);

        // Deduct coins
        WalletTransactionResponseDto walletTransactionResponseDto = debitForBusiness(
                userId,
                request.getAmountCoins(),
                "Cashout request: " + request.getType(),
                "CASHOUT-" + (request.getTransactionId() != null ? request.getTransactionId() : UUID.randomUUID().toString()),
                "IDEM-CASHOUT-" + (request.getTransactionId() != null ? request.getTransactionId() : UUID.randomUUID().toString())
        );

        log.info("Cashout request of {} coins (₹{}) submitted for user {}", request.getAmountCoins(), rupees, userId);
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

        UserEntity user = users.findByIdForUpdate(userId)
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

    private UserEntity findUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
        }
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "App user not found"));
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

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            log.warn("Unauthorized access attempt without valid authentication principal in SecurityContext");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
