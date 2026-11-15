package com.fitnessapp.gamify.gamification.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Content shown on the game screen when the user taps a game: rules, tips, GIF and video. */
@Entity
@Table(name = "app_game_details")
@Getter
@NoArgsConstructor
public class GameDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalogue_id", nullable = false, unique = true)
    private GameCatalogueEntity catalogue;

    @Column(name = "gif_url", length = 500)
    private String gifUrl;
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "app_game_detail_rules", joinColumns = @JoinColumn(name = "detail_id"))
    @OrderColumn(name = "rule_order")
    @Column(name = "rule_text", nullable = false, length = 500)
    private List<String> rules = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "app_game_detail_tips", joinColumns = @JoinColumn(name = "detail_id"))
    @OrderColumn(name = "tip_order")
    @Column(name = "tip_text", nullable = false, length = 500)
    private List<String> tips = new ArrayList<>();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public GameDetailEntity(GameCatalogueEntity catalogue, String gifUrl, String videoUrl,
                            List<String> rules, List<String> tips) {
        this.catalogue = catalogue;
        this.gifUrl = gifUrl;
        this.videoUrl = videoUrl;
        this.rules = new ArrayList<>(rules);
        this.tips = new ArrayList<>(tips);
    }
}