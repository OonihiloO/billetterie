// SPDX-License-Identifier: Apache-2.0
package org.montrealjug.billetterie.entity;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import org.hibernate.proxy.HibernateProxy;

@Embeddable
public class ActivityParticipantKey {

    private long activityId;
    private long participantId;

    public long getActivityId() {
        return activityId;
    }

    public void setActivityId(long activityId) {
        this.activityId = activityId;
    }

    public long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(long participantId) {
        this.participantId = participantId;
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
        ActivityParticipantKey that = (ActivityParticipantKey) o;
        return (
            Objects.equals(getActivityId(), that.getActivityId()) &&
            Objects.equals(getParticipantId(), that.getParticipantId())
        );
    }

    @Override
    public final int hashCode() {
        return Objects.hash(activityId, participantId);
    }
}
