package com.fitnessapp.gamify.gamification.model.entity;

import com.fitnessapp.gamify.gamification.model.enums.DurationUnit;
import com.fitnessapp.gamify.gamification.model.enums.GameLevel;
import com.fitnessapp.gamify.gamification.model.enums.GameType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "app_game_catalogue",
        uniqueConstraints = @UniqueConstraint(name = "uk_game_catalogue_type", columnNames = "game_type"),
        indexes = @Index(name = "idx_game_catalogue_active_order", columnList = "active, display_order"))
@Getter
@NoArgsConstructor
public class GameCatalogueEntity {
    private static final Duration NEW_BADGE_WINDOW = Duration.ofDays(14);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false, length = 40)
    private GameType gameType;
    @Column(nullable = false, length = 120)
    private String title;                       // header
    @Column(name = "sub_title", length = 200)
    private String subTitle;                    // sub header
    @Column(nullable = false, length = 1000)
    private String description;
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 20)
    private GameLevel level;

    /** Target reps. Null for time-based games such as plank. */
    @Column(name = "total_reps_required")
    private Integer totalRepsRequired;

    @Column(name = "duration_value", nullable = false)
    private int durationValue;
    @Enumerated(EnumType.STRING)
    @Column(name = "duration_unit", nullable = false, length = 10)
    private DurationUnit durationUnit;

    @Column(nullable = false)
    private boolean paid;
    /** Coins needed to play. Always 0 when the game is free. */
    @Column(name = "entry_fee_coins", nullable = false)
    private long entryFeeCoins;
    /** Coins the user can win. */
    @Column(name = "reward_coins", nullable = false)
    private long rewardCoins;
    /** XP the user can earn. */
    @Column(name = "reward_xp", nullable = false)
    private long rewardXp;

    @Column(name = "max_reps_per_minute", nullable = false)
    private int maxRepsPerMinute = 180;

    /** "New" badge is derived from this, so nobody has to flip a flag later. */
    @Column(name = "released_at", nullable = false)
    private Instant releasedAt;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void create() {
        if (createdAt == null) createdAt = Instant.now();
        if (releasedAt == null) releasedAt = createdAt;
    }

    @Builder
    public GameCatalogueEntity(GameType gameType, String title, String subTitle, String description,
                               String imageUrl, GameLevel level, Integer totalRepsRequired,
                               int durationValue, DurationUnit durationUnit, boolean paid,
                               long entryFeeCoins, long rewardCoins, long rewardXp,
                               int maxRepsPerMinute, Instant releasedAt, int displayOrder) {
        this.gameType = gameType;
        this.title = title;
        this.subTitle = subTitle;
        this.description = description;
        this.imageUrl = imageUrl;
        this.level = level;
        this.totalRepsRequired = totalRepsRequired;
        this.durationValue = durationValue;
        this.durationUnit = durationUnit;
        this.paid = paid;
        this.entryFeeCoins = paid ? Math.max(0, entryFeeCoins) : 0;
        this.rewardCoins = Math.max(0, rewardCoins);
        this.rewardXp = Math.max(0, rewardXp);
        this.maxRepsPerMinute = maxRepsPerMinute > 0 ? maxRepsPerMinute : 180;
        this.releasedAt = releasedAt;
        this.displayOrder = displayOrder;
        this.active = true;
    }

    public boolean isNewGame(Instant now) {
        return releasedAt != null && releasedAt.plus(NEW_BADGE_WINDOW).isAfter(now);
    }

    public void deactivate() {
        this.active = false;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTitle(String title) { this.title = title; }
    public void setSubTitle(String subTitle) { this.subTitle = subTitle; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setLevel(GameLevel level) { this.level = level; }
    public void setTotalRepsRequired(Integer totalRepsRequired) { this.totalRepsRequired = totalRepsRequired; }
    public void setDurationValue(int durationValue) { this.durationValue = durationValue; }
    public void setDurationUnit(DurationUnit durationUnit) { this.durationUnit = durationUnit; }
    public void setPaid(boolean paid) {
        this.paid = paid;
        if (!paid) this.entryFeeCoins = 0;
    }
    public void setEntryFeeCoins(long entryFeeCoins) { this.entryFeeCoins = paid ? Math.max(0, entryFeeCoins) : 0; }
    public void setRewardCoins(long rewardCoins) { this.rewardCoins = Math.max(0, rewardCoins); }
    public void setRewardXp(long rewardXp) { this.rewardXp = Math.max(0, rewardXp); }
    public void setMaxRepsPerMinute(int maxRepsPerMinute) { this.maxRepsPerMinute = maxRepsPerMinute > 0 ? maxRepsPerMinute : 180; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}