package com.fitnessapp.followUsLinks;

import java.util.List;
import java.util.Map;

public interface FollowUsService {
    List<FollowUsLinksResponseDto> getAppLinks();

    Map<String, String> uploadAppLinksUrl(List<FollowUsLinksRequestDto> dtoList);

    Map<String, String> patchAppLinksUrl(FollowUsLinksRequestDto dto);
}
