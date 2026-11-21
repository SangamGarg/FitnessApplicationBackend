package com.fitnessapp.followUsLinks.service;

import com.fitnessapp.followUsLinks.model.FollowUsLinksPatchRequestDto;
import com.fitnessapp.followUsLinks.model.FollowUsLinksRequestDto;
import com.fitnessapp.followUsLinks.model.FollowUsLinksResponseDto;
import com.fitnessapp.followUsLinks.model.PortalFollowUsLinksResponseDto;

import java.util.List;
import java.util.Map;

public interface FollowUsService {

    List<FollowUsLinksResponseDto> getAppLinks();

    Map<String, String> uploadAppLinksUrl(
            List<FollowUsLinksRequestDto> dtoList
    );

    Map<String, String> patchAppLinksUrl(
            Long id,
            FollowUsLinksPatchRequestDto dto
    );

    List<PortalFollowUsLinksResponseDto> getAppLinksForPortal();
}