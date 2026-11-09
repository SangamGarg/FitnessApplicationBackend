package com.fitnessapp.followUsLinks;

import com.fitnessapp.followUsLinks.FollowUsLinksEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowUsServiceImpl implements FollowUsService {

    private final FollowUsRepo followUsRepo;

    @Override
    @Transactional(readOnly = true)
    public List<FollowUsLinksResponseDto> getAppLinks() {
        log.debug("Fetching all App Links");
        return followUsRepo.findAll().stream()
                .map(link -> new FollowUsLinksResponseDto(link.getId(), link.getTitle(), link.getUrl()))
                .toList();
    }

    @Override
    @Transactional
    public Map<String, String> uploadAppLinksUrl(List<FollowUsLinksRequestDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide at least one app link");
        }

        log.info("Adding {} app link(s)", dtoList.size());
        
        List<FollowUsLinksEntity> existingLinks = followUsRepo.findAll();

        // Compare trimmed, case-insensitive titles and URLs to avoid accidental duplicates.
        var existingTitles = new java.util.HashSet<String>();
        var existingUrls = new java.util.HashSet<String>();
        existingLinks.forEach(link -> {
            existingTitles.add(normalizeDuplicateKey(link.getTitle()));
            existingUrls.add(normalizeDuplicateKey(link.getUrl()));
        });

        List<FollowUsLinksEntity> linksToAdd = new ArrayList<>();
        for (FollowUsLinksRequestDto dto : dtoList) {
            if (dto == null || dto.getTitle() == null || dto.getUrl() == null
                    || dto.getTitle().isBlank() || dto.getUrl().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each app link must have a title and URL");
            }

            String title = dto.getTitle().trim();
            String url = dto.getUrl().trim();
            String titleKey = normalizeDuplicateKey(title);
            String urlKey = normalizeDuplicateKey(url);

            if (!existingTitles.add(titleKey)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "An app link with this title already exists: " + title);
            }
            if (!existingUrls.add(urlKey)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "An app link with this URL already exists: " + url);
            }

            linksToAdd.add(FollowUsLinksEntity.builder().title(title).url(url).build());
        }

        followUsRepo.saveAll(linksToAdd);

        log.info("Added {} app link(s)", linksToAdd.size());
        return Map.of("message", linksToAdd.size() + " app link(s) added successfully");
    }

    private String normalizeDuplicateKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    @Transactional
    public Map<String, String> patchAppLinksUrl(FollowUsLinksRequestDto dto) {
        log.info("Patching app link with title: [{}]", dto.getTitle());

        if (dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getUrl() == null || dto.getUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link title and URL are required");
        }

        String requestedTitle = dto.getTitle().trim();
        String requestedUrl = dto.getUrl().trim();

        List<FollowUsLinksEntity> allLinks = followUsRepo.findAll();

        FollowUsLinksEntity target = allLinks.stream()
                .filter(link -> link.getTitle() != null && link.getTitle().trim().equalsIgnoreCase(requestedTitle))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Link with title [{}] not found for update", requestedTitle);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Link title not found");
                });

        boolean duplicateUrl = allLinks.stream()
                .filter(link -> !Objects.equals(link.getId(), target.getId()))
                .anyMatch(link -> normalizeDuplicateKey(link.getUrl()).equals(normalizeDuplicateKey(requestedUrl)));
        if (duplicateUrl) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another app link already uses this URL");
        }

        target.setUrl(requestedUrl);
        followUsRepo.save(target);
        
        log.info("Link [{}] updated successfully", dto.getTitle());
        return Map.of("message", "Link updated successfully");
    }
}
