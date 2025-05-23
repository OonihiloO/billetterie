// SPDX-License-Identifier: Apache-2.0
package org.montrealjug.billetterie.ui;

import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.montrealjug.billetterie.entity.Booker;
import org.montrealjug.billetterie.entity.Participant;

public record PresentationEvent(
    Long id,
    String title,
    String description,
    LocalDate date,
    List<PresentationActivity> activities,
    Boolean active,
    @Nullable String imagePath,
    String location
) {
    public record ActivityParticipant(long activityId, PresentationParticipant participant) {
        public String formValue() {
            return "%d-%d".formatted(activityId, participant.id());
        }
        public long id() {
            return participant.id();
        }

        public String firstName() {
            return participant.firstName();
        }
        public String lastName() {
            return participant.lastName();
        }

        public int age() {
            return participant.age();
        }
    }

    public List<ActivityParticipant> alreadyBookedParticipants(Booker booker, PresentationActivity activity) {
        return this.activities.stream()
            .filter(a -> a.id() != activity.id())
            .flatMap(a -> a.allParticipants(booker).stream().map(p -> new ActivityParticipant(a.id(), p)))
            .toList();
    }

    public List<Participant> nonBookedParticipants(Booker booker) {
        var bookedParticipantIds =
            this.activities.stream()
                .flatMap(a -> a.allParticipants(booker).stream())
                .map(PresentationParticipant::id)
                .collect(Collectors.toSet());
        return booker.getParticipants().stream().filter(p -> !bookedParticipantIds.contains(p.getId())).toList();
    }

    public Map<Long, Set<Long>> bookedParticipants(Booker booker) {
        return this.activities.stream()
            .flatMap(a -> a.allParticipants(booker).stream().map(p -> new ActivityParticipant(a.id(), p)))
            .collect(
                Collectors.groupingBy(
                    ActivityParticipant::activityId,
                    HashMap::new,
                    Collectors.mapping(ActivityParticipant::id, Collectors.toSet())
                )
            );
    }

    public List<PresentationParticipant> participantsForBooker(Booker booker) {
        return this.activities.stream().flatMap(a -> a.allParticipants(booker).stream()).toList();
    }

    public PresentationActivity activity(long activityId) {
        return this.activities.stream()
            .filter(p -> p.id() == activityId)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid activity id " + activityId));
    }
}
