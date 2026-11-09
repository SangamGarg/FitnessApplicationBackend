package com.fitnessapp.exercise.app.service;

import com.fitnessapp.exercise.app.model.dto.response.*;
import com.fitnessapp.exercise.app.repo.WorkoutAnalyticsRepository;
import com.fitnessapp.exercise.app.repo.WorkoutAnalyticsRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutAnalyticsService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private final WorkoutAnalyticsRepository repository;

    public WorkoutAnalyticsOverviewResponse overview(LocalDate from, LocalDate to, ZoneId zone) {
        return overviewForUser(currentUserId(), from, to, zone);
    }

    public List<PersonalRecordResponse> personalRecords(ZoneId zone) {
        DateRange allTime = new DateRange(LocalDate.of(1970, 1, 1), LocalDate.now(zone),
                Instant.EPOCH, Instant.now().plusSeconds(1));
        List<WorkoutAnalyticsRow> rows = rows(allTime, null);
        Map<Long, List<WorkoutAnalyticsRow>> byExercise = uniqueSets(rows).values().stream()
                .collect(Collectors.groupingBy(WorkoutAnalyticsRow::getExerciseId));
        List<PersonalRecordResponse> result = new ArrayList<>();
        for (List<WorkoutAnalyticsRow> exerciseRows : byExercise.values()) {
            WorkoutAnalyticsRow first = exerciseRows.get(0);
            RecordCandidate weight = new RecordCandidate();
            RecordCandidate reps = new RecordCandidate();
            RecordCandidate duration = new RecordCandidate();
            RecordCandidate oneRm = new RecordCandidate();
            for (WorkoutAnalyticsRow row : exerciseRows) {
                BigDecimal w = row.getWeightKg();
                if (w != null && w.compareTo(ZERO) > 0 && (weight.value == null || w.compareTo(weight.value) > 0))
                    weight = new RecordCandidate(w, row.getCompletedAt());
                Integer r = row.getRepsCompleted();
                if (r != null && (reps.value == null || BigDecimal.valueOf(r).compareTo(reps.value) > 0))
                    reps = new RecordCandidate(BigDecimal.valueOf(r), row.getCompletedAt());
                Integer seconds = row.getSetDurationSeconds();
                if (seconds != null && (duration.value == null || BigDecimal.valueOf(seconds).compareTo(duration.value) > 0))
                    duration = new RecordCandidate(BigDecimal.valueOf(seconds), row.getCompletedAt());
                if (w != null && w.compareTo(ZERO) > 0 && r != null && r > 0 && r <= 10) {
                    BigDecimal estimate = w.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(r)
                            .divide(BigDecimal.valueOf(30), 6, RoundingMode.HALF_UP)));
                    if (oneRm.value == null || estimate.compareTo(oneRm.value) > 0)
                        oneRm = new RecordCandidate(estimate, row.getCompletedAt());
                }
            }
            result.add(new PersonalRecordResponse(first.getExerciseId(), first.getExerciseKey(),
                    first.getExerciseName(), record(weight, "kg"), record(reps, "reps"),
                    record(duration, "seconds"), record(oneRm, "kg")));
        }
        result.sort(Comparator.comparing(PersonalRecordResponse::exerciseName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public Page<ExercisePerformanceHistoryResponse.PerformancePoint> exerciseHistory(
            Long exerciseId, LocalDate from, LocalDate to, ZoneId zone, int page, int size) {
        DateRange range = range(from, to, zone, 730);
        List<WorkoutAnalyticsRow> rows = rows(range, exerciseId);
        Map<Long, List<WorkoutAnalyticsRow>> byWorkout = uniqueSets(rows).values().stream()
                .collect(Collectors.groupingBy(WorkoutAnalyticsRow::getWorkoutId));
        List<ExercisePerformanceHistoryResponse.PerformancePoint> points = new ArrayList<>();
        for (List<WorkoutAnalyticsRow> workoutRows : byWorkout.values()) {
            WorkoutAnalyticsRow first = workoutRows.get(0);
            long totalReps = 0;
            BigDecimal volume = ZERO;
            BigDecimal bestWeight = null;
            BigDecimal bestOneRm = null;
            for (WorkoutAnalyticsRow row : workoutRows) {
                totalReps += nullToZero(row.getRepsCompleted());
                volume = volume.add(setVolume(row));
                if (row.getWeightKg() != null && (bestWeight == null || row.getWeightKg().compareTo(bestWeight) > 0))
                    bestWeight = row.getWeightKg();
                if (row.getWeightKg() != null && row.getRepsCompleted() != null
                        && row.getRepsCompleted() > 0 && row.getRepsCompleted() <= 10) {
                    BigDecimal estimate = row.getWeightKg().multiply(BigDecimal.ONE.add(
                            BigDecimal.valueOf(row.getRepsCompleted()).divide(BigDecimal.valueOf(30), 6, RoundingMode.HALF_UP)));
                    if (bestOneRm == null || estimate.compareTo(bestOneRm) > 0) bestOneRm = estimate;
                }
            }
            points.add(new ExercisePerformanceHistoryResponse.PerformancePoint(
                    first.getWorkoutId(), first.getCompletedAt(), first.getWorkoutDurationSeconds(),
                    workoutRows.size(), totalReps, scale(volume), scaleNullable(bestWeight), scaleNullable(bestOneRm)));
        }
        points.sort(Comparator.comparing(ExercisePerformanceHistoryResponse.PerformancePoint::completedAt).reversed());
        long offset = (long) page * size;
        int start = (int) Math.min(offset, points.size());
        int end = Math.min(start + size, points.size());
        return new PageImpl<>(points.subList(start, end), PageRequest.of(page, size), points.size());
    }

    public ExercisePerformanceHistoryResponse exerciseHistoryEnvelope(Long exerciseId, LocalDate from,
            LocalDate to, ZoneId zone, int page, int size) {
        DateRange range = range(from, to, zone, 730);
        List<WorkoutAnalyticsRow> rows = rows(range, exerciseId);
        if (rows.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No completed workout history found for this exercise");
        WorkoutAnalyticsRow first = rows.get(0);
        Page<ExercisePerformanceHistoryResponse.PerformancePoint> result =
                exerciseHistory(exerciseId, from, to, zone, page, size);
        return new ExercisePerformanceHistoryResponse(exerciseId, first.getExerciseKey(), first.getExerciseName(),
                result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public MuscleGroupVolumeResponse muscleGroupVolume(LocalDate from, LocalDate to, ZoneId zone) {
        DateRange range = range(from, to, zone, 730);
        List<WorkoutAnalyticsRow> rows = rows(range, null);
        Map<Long, MuscleAccumulator> groups = new HashMap<>();
        Map<String, WorkoutAnalyticsRow> primarySetMuscle = new HashMap<>();
        for (WorkoutAnalyticsRow row : rows) {
            if (row.getMuscleGroupId() == null || !"PRIMARY".equalsIgnoreCase(row.getMuscleRole())) continue;
            primarySetMuscle.putIfAbsent(row.getSetId() + ":" + row.getMuscleGroupId(), row);
        }
        for (WorkoutAnalyticsRow row : primarySetMuscle.values()) {
            MuscleAccumulator acc = groups.computeIfAbsent(row.getMuscleGroupId(), ignored -> new MuscleAccumulator(row));
            acc.sets++;
            acc.reps += nullToZero(row.getRepsCompleted());
            acc.volume = acc.volume.add(setVolume(row));
            acc.workouts.add(row.getWorkoutId());
        }
        List<MuscleGroupVolumeResponse.MuscleGroupMetric> metrics = groups.values().stream()
                .map(a -> new MuscleGroupVolumeResponse.MuscleGroupMetric(a.id, a.key, a.name,
                        a.sets, a.reps, scale(a.volume), a.workouts.size()))
                .sorted(Comparator.comparing(MuscleGroupVolumeResponse.MuscleGroupMetric::volumeKg).reversed())
                .toList();
        return new MuscleGroupVolumeResponse(range.from, range.to, metrics);
    }

    public WorkoutTrendsResponse trends(LocalDate from, LocalDate to, ZoneId zone, String period) {
        DateRange range = range(from, to, zone, 730);
        String normalized = period == null ? "WEEKLY" : period.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("WEEKLY") && !normalized.equals("MONTHLY"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "period must be WEEKLY or MONTHLY");
        List<WorkoutAnalyticsRow> rows = rows(range, null);
        Map<LocalDate, TrendAccumulator> buckets = new TreeMap<>();
        Map<Long, WorkoutAnalyticsRow> workoutRows = new HashMap<>();
        Map<Long, WorkoutAnalyticsRow> sets = uniqueSets(rows);
        for (WorkoutAnalyticsRow row : rows) workoutRows.putIfAbsent(row.getWorkoutId(), row);
        for (WorkoutAnalyticsRow row : workoutRows.values()) {
            LocalDate date = row.getCompletedAt().atZone(zone).toLocalDate();
            LocalDate key = normalized.equals("WEEKLY")
                    ? date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    : date.withDayOfMonth(1);
            buckets.computeIfAbsent(key, ignored -> new TrendAccumulator()).workouts++;
            buckets.get(key).duration += nullToZero(row.getWorkoutDurationSeconds());
        }
        for (WorkoutAnalyticsRow row : sets.values()) {
            LocalDate date = row.getCompletedAt().atZone(zone).toLocalDate();
            LocalDate key = normalized.equals("WEEKLY")
                    ? date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    : date.withDayOfMonth(1);
            TrendAccumulator acc = buckets.computeIfAbsent(key, ignored -> new TrendAccumulator());
            acc.sets++;
            acc.reps += nullToZero(row.getRepsCompleted());
            acc.volume = acc.volume.add(setVolume(row));
        }
        List<WorkoutTrendsResponse.TrendPoint> points = buckets.entrySet().stream()
                .map(e -> new WorkoutTrendsResponse.TrendPoint(e.getKey(), e.getValue().workouts,
                        e.getValue().duration, e.getValue().sets, e.getValue().reps, scale(e.getValue().volume)))
                .toList();
        return new WorkoutTrendsResponse(normalized, range.from, range.to, points);
    }

    public WorkoutAnalyticsOverviewResponse overviewForUser(String userId, LocalDate from, LocalDate to, ZoneId zone) {
        DateRange range = range(from, to, zone, 730);
        List<WorkoutAnalyticsRow> rows = repository.findCompletedSetRows(userId, range.fromInstant, range.toExclusive, null);
        Set<Long> workoutIds = new HashSet<>();
        Set<LocalDate> activeDays = new HashSet<>();
        Map<Long, WorkoutAnalyticsRow> workoutRows = new HashMap<>();
        Map<Long, WorkoutAnalyticsRow> sets = uniqueSets(rows);
        long reps = 0, duration = 0;
        BigDecimal volume = ZERO;
        for (WorkoutAnalyticsRow row : rows) {
            workoutIds.add(row.getWorkoutId());
            workoutRows.putIfAbsent(row.getWorkoutId(), row);
            activeDays.add(row.getCompletedAt().atZone(zone).toLocalDate());
        }
        for (WorkoutAnalyticsRow row : workoutRows.values()) duration += nullToZero(row.getWorkoutDurationSeconds());
        for (WorkoutAnalyticsRow row : sets.values()) {
            reps += nullToZero(row.getRepsCompleted());
            volume = volume.add(setVolume(row));
        }
        return new WorkoutAnalyticsOverviewResponse(range.from, range.to, workoutIds.size(), duration,
                workoutIds.isEmpty() ? 0 : duration / workoutIds.size(), sets.size(), reps, scale(volume), activeDays.size());
    }

    private List<WorkoutAnalyticsRow> rows(DateRange range, Long exerciseId) {
        return repository.findCompletedSetRows(currentUserId(), range.fromInstant, range.toExclusive, exerciseId);
    }

    private static Map<Long, WorkoutAnalyticsRow> uniqueSets(List<WorkoutAnalyticsRow> rows) {
        Map<Long, WorkoutAnalyticsRow> unique = new LinkedHashMap<>();
        for (WorkoutAnalyticsRow row : rows) unique.putIfAbsent(row.getSetId(), row);
        return unique;
    }

    private static BigDecimal setVolume(WorkoutAnalyticsRow row) {
        if (row.getWeightKg() == null || row.getRepsCompleted() == null) return ZERO;
        return row.getWeightKg().multiply(BigDecimal.valueOf(row.getRepsCompleted()));
    }

    private static int nullToZero(Integer value) { return value == null ? 0 : value; }
    private static BigDecimal scale(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
    private static BigDecimal scaleNullable(BigDecimal value) { return value == null ? null : scale(value); }

    private static PersonalRecordResponse.RecordValue record(RecordCandidate c, String unit) {
        return c.value == null ? null : new PersonalRecordResponse.RecordValue(scale(c.value), unit, c.at);
    }

    private DateRange range(LocalDate from, LocalDate to, ZoneId zone, int maxDays) {
        LocalDate end = to == null ? LocalDate.now(zone) : to;
        LocalDate start = from == null ? end.minusDays(29) : from;
        if (start.isAfter(end)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must be on or before 'to'");
        if (Duration.between(start.atStartOfDay(zone), end.plusDays(1).atStartOfDay(zone)).toDays() > maxDays)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date range cannot exceed " + maxDays + " days");
        return new DateRange(start, end, start.atStartOfDay(zone).toInstant(), end.plusDays(1).atStartOfDay(zone).toInstant());
    }

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || "anonymousUser".equals(auth.getName()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return auth.getName();
    }

    private record DateRange(LocalDate from, LocalDate to, Instant fromInstant, Instant toExclusive) {}
    private static class RecordCandidate {
        BigDecimal value; Instant at;
        RecordCandidate() {}
        RecordCandidate(BigDecimal value, Instant at) { this.value = value; this.at = at; }
    }
    private static class MuscleAccumulator {
        Long id; String key, name; long sets, reps; BigDecimal volume = ZERO; Set<Long> workouts = new HashSet<>();
        MuscleAccumulator(WorkoutAnalyticsRow r) { id=r.getMuscleGroupId(); key=r.getMuscleGroupKey(); name=r.getMuscleGroupName(); }
    }
    private static class TrendAccumulator {
        long workouts, duration, sets, reps; BigDecimal volume = ZERO;
    }
}
