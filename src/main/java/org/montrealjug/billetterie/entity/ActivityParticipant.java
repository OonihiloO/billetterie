// SPDX-License-Identifier: Apache-2.0
package org.montrealjug.billetterie.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import java.time.Instant;
import java.util.Objects;
import org.hibernate.proxy.HibernateProxy;

@Entity
public class ActivityParticipant implements Comparable<ActivityParticipant> {

    @EmbeddedId
    private ActivityParticipantKey activityParticipantKey = new ActivityParticipantKey();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("activityId")
    private Activity activity;

    @ManyToOne
    @MapsId("participantId")
    private Participant participant;

    @Column(nullable = false, updatable = false)
    private Instant registrationTime = Instant.now();

    private Instant confirmationTime;
    private Instant checkInTime;

    public ActivityParticipantKey getActivityParticipantKey() {
        return activityParticipantKey;
    }

    public void setActivityParticipantKey(ActivityParticipantKey activityParticipantKey) {
        this.activityParticipantKey = activityParticipantKey;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public Instant getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(Instant registrationTime) {
        this.registrationTime = registrationTime;
    }

    public Instant getConfirmationTime() {
        return confirmationTime;
    }

    public void setConfirmationTime(Instant confirmationTime) {
        this.confirmationTime = confirmationTime;
    }

    public Instant getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Instant checkInTime) {
        this.checkInTime = checkInTime;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ActivityParticipant that = (ActivityParticipant) o;
        return (
            getActivityParticipantKey() != null &&
            Objects.equals(getActivityParticipantKey(), that.getActivityParticipantKey())
        );
    }

    @Override
    public final int hashCode() {
        return Objects.hash(activityParticipantKey);
    }

    @Override
    public int compareTo(ActivityParticipant o) {
        if (this.equals(o)) {
            return 0;
        }
        var activityComp = Long.compare(
            this.activityParticipantKey.getActivityId(),
            o.activityParticipantKey.getActivityId()
        );
        if (activityComp == 0) {
            return this.registrationTime.compareTo(o.registrationTime);
        } else {
            return activityComp;
        }
    }
}
