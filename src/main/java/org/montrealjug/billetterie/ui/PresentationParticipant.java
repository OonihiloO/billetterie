// SPDX-License-Identifier: Apache-2.0
package org.montrealjug.billetterie.ui;

import java.time.Instant;
import java.time.LocalDate;
import org.montrealjug.billetterie.entity.Booker;
import org.montrealjug.billetterie.entity.Participant;

public record PresentationParticipant(
    long id,
    String firstName,
    String lastName,
    int age,
    Booker booker,
    boolean isWaiting,
    Instant checkInTime
) {
    public PresentationParticipant(Participant participant, boolean isWaiting) {
        this(participant, isWaiting, null);
    }

    public PresentationParticipant(Participant participant, boolean isWaiting, Instant checkInTime) {
        this(
            participant.getId(),
            participant.getFirstName(),
            participant.getLastName(),
            LocalDate.now().getYear() - participant.getYearOfBirth(),
            participant.getBooker(),
            isWaiting,
            checkInTime
        );
    }
}
