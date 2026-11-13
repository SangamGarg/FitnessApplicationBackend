package com.fitnessapp.support.chatbot.knowledge;

import com.fitnessapp.support.models.entity.SupportFaqEntity;
import com.fitnessapp.support.repo.SupportFaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaqRetrievalService {

    private static final int MAX_RESULTS = 5;
    private static final int CANDIDATES_PER_TERM = 10;

    private static final Pattern NON_WORD =
            Pattern.compile("[^a-zA-Z0-9]+");

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "are", "you", "your",
            "how", "what", "when", "where", "why", "can",
            "does", "this", "that", "with", "from", "have",
            "has", "was", "were", "will", "would", "could",
            "should", "not", "but", "about", "into", "please",
            "help", "app", "use", "using", "my", "me", "is",
            "it", "to", "of", "in", "on", "a", "an", "i"
    );

    private final SupportFaqRepository faqRepository;

    @Transactional(readOnly = true)
    public List<RetrievedFaq> retrieveRelevantFaqs(String userQuestion) {

        if (userQuestion == null || userQuestion.isBlank()) {
            return List.of();
        }

        List<String> terms = extractSearchTerms(userQuestion);

        if (terms.isEmpty()) {
            return List.of();
        }

        Map<Long, ScoredFaq> candidates = new HashMap<>();

        for (String term : terms) {
            List<SupportFaqEntity> matches = faqRepository.searchActiveFaqs(
                    term,
                    PageRequest.of(0, CANDIDATES_PER_TERM)
            );

            for (SupportFaqEntity faq : matches) {
                int score = calculateScore(faq, term);

                candidates.merge(
                        faq.getId(),
                        new ScoredFaq(faq, score),
                        (existing, incoming) -> new ScoredFaq(
                                existing.faq(),
                                existing.score() + incoming.score()
                        )
                );
            }
        }

        return candidates.values()
                .stream()
                .sorted(
                        Comparator.comparingInt(ScoredFaq::score)
                                .reversed()
                                .thenComparing(
                                        scored -> !scored.faq().isPopular()
                                )
                                .thenComparingInt(
                                        scored -> scored.faq().getSortOrder()
                                )
                )
                .limit(MAX_RESULTS)
                .map(this::toRetrievedFaq)
                .toList();
    }

    private List<String> extractSearchTerms(String question) {

        return Arrays.stream(
                        NON_WORD.matcher(question.toLowerCase(Locale.ROOT))
                                .replaceAll(" ")
                                .trim()
                                .split("\\s+")
                )
                .filter(term -> term.length() >= 3)
                .filter(term -> !STOP_WORDS.contains(term))
               // .filter(scored -> scored.score() >= 2)
                .distinct()
                .limit(8)
                .toList();
    }

    private int calculateScore(SupportFaqEntity faq, String term) {

        String normalizedQuestion =
                faq.getQuestion().toLowerCase(Locale.ROOT);

        String normalizedAnswer =
                faq.getAnswer().toLowerCase(Locale.ROOT);

        int score = 0;

        if (normalizedQuestion.contains(term)) {
            score += 3;
        }

        if (normalizedAnswer.contains(term)) {
            score += 1;
        }

//        if (faq.isPopular()) {
//            score += 1;
//        }

        return score;
    }

    private RetrievedFaq toRetrievedFaq(ScoredFaq scoredFaq) {

        SupportFaqEntity faq = scoredFaq.faq();

        return new RetrievedFaq(
                faq.getId(),
                faq.getCategory().getCategoryKey(),
                faq.getCategory().getName(),
                faq.getQuestion(),
                faq.getAnswer(),
                scoredFaq.score()
        );
    }

    private record ScoredFaq(
            SupportFaqEntity faq,
            int score
    ) {
    }
}