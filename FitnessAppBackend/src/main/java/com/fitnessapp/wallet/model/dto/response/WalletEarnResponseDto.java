package com.fitnessapp.wallet.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletEarnResponseDto {
    private String title;
    private String subtitle;
    private String description;
    private ReferralBoxDto referralBox;
    private List<ReferralFriendDto> referredFriends;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferralBoxDto {
        private String header;
        private String referralCode;
        private long totalEarnedMpCoins;
        private String friendInvitedText;
        private int totalFriendsInvited;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferralFriendDto {
        private String name;
        private String status;
        private long earnedCoins;
    }
}
