// SPDX-License-Identifier: Apache-2.0
package org.montrealjug.billetterie.ui;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalTime;
import java.util.List;
import org.montrealjug.billetterie.entity.Activity.RegistrationStatus;
import org.montrealjug.billetterie.entity.Booker;

public record PresentationActivity(
    long id,
    @NotBlank String title,
    @NotBlank String description,
    int maxParticipants,
    int maxWaitingQueue,
    List<PresentationParticipant> allParticipants,
    LocalTime time,
    String imagePath
) {
    public List<PresentationParticipant> allParticipants(Booker booker) {
        // this value will be mutated is some cases, we can't use toList()
        return allParticipants.stream().filter(p -> p.booker().equals(booker)).toList();
    }

    public List<PresentationParticipant> participants(Booker booker) {
        return allParticipants.stream().filter(p -> p.booker().equals(booker) && !p.isWaiting()).toList();
    }

    public List<PresentationParticipant> waitingParticipants(Booker booker) {
        return allParticipants.stream().filter(p -> p.booker().equals(booker) && p.isWaiting()).toList();
    }

    public List<PresentationParticipant> participants() {
        return allParticipants.stream().filter(p -> !p.isWaiting()).toList();
    }

    public List<PresentationParticipant> waitingParticipants() {
        return allParticipants.stream().filter(PresentationParticipant::isWaiting).toList();
    }

    public RegistrationStatus registrationStatus() {
        return RegistrationStatus.from(allParticipants.size(), maxParticipants, maxWaitingQueue);
    }

    public int availableSeats(Booker booker) {
        return maxParticipants + maxWaitingQueue + allParticipants(booker).size() - allParticipants.size();
    }
}
