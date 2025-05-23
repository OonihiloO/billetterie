// SPDX-License-Identifier: Apache-2.0
package org.montrealjug.billetterie.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.montrealjug.billetterie.entity.Activity;
import org.montrealjug.billetterie.entity.ActivityParticipant;
import org.montrealjug.billetterie.entity.Booker;
import org.montrealjug.billetterie.entity.Event;
import org.montrealjug.billetterie.entity.Participant;
import org.montrealjug.billetterie.repository.EventRepository;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final EventRepository eventRepository;
    private final Lock lock;

    public BookingService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
        // Just a fair Lock for now
        // a more sophisticated and robust approach with a `ConcurrentQueue` and a `Future` result
        // could be implemented later if concurrency and throughput are an issue:
        // on call of the method a request is put in the queue, a virtual thread is used to consume the queued requests
        // and return the `Future` result when done
        this.lock = new ReentrantLock(true);
    }

    public record UpdatedBooking(Event event, Set<Booker> bookersToNotify) {}

    public UpdatedBooking saveBookings(Booker booker, Event event, Map<Long, Set<Long>> participants)
        throws InterruptedException {
        // ensure that bookings are saved in the same order they are received
        // `tryLock()` without timeout is not fair, according to Javadoc,
        // so use 1 minute as timeout
        if (this.lock.tryLock(1L, TimeUnit.MINUTES)) {
            try {
                var bookerParticipantById = booker
                    .getParticipants()
                    .stream()
                    .collect(Collectors.toMap(Participant::getId, Function.identity()));
                var bookersToNotify = new HashSet<Booker>();
                for (var activity : event.getActivities()) {
                    var activityParticipants = participants.getOrDefault(activity.getId(), Collections.emptySet());
                    var registrationStatus = activity.getRegistrationStatus();
                    var existingParticipants = activity.getNonWaitingParticipants();
                    activity
                        .getParticipants()
                        .removeIf(ap ->
                            ap.getParticipant().getBooker().equals(booker) &&
                            !activityParticipants.contains(ap.getActivityParticipantKey().getParticipantId())
                        );
                    if (registrationStatus != Activity.RegistrationStatus.OPEN) {
                        var newParticipants = new ArrayList<>(activity.getNonWaitingParticipants());
                        if (!existingParticipants.equals(newParticipants)) {
                            newParticipants.removeAll(existingParticipants);
                            newParticipants
                                .stream()
                                .map(ActivityParticipant::getParticipant)
                                .map(Participant::getBooker)
                                .forEach(bookersToNotify::add);
                        }
                    }
                    var existingParticipantIds = activity
                        .getParticipants()
                        .stream()
                        .map(ap -> ap.getActivityParticipantKey().getParticipantId())
                        .collect(Collectors.toSet());
                    activityParticipants
                        .stream()
                        .filter(Predicate.not(existingParticipantIds::contains))
                        .forEach(participantId -> {
                            if (activity.getRegistrationStatus() == Activity.RegistrationStatus.CLOSED) {
                                throw new IllegalArgumentException(
                                    "Registration is closed for Activity " + activity.getId()
                                );
                            }
                            var participant = bookerParticipantById.get(participantId);
                            if (participant == null) {
                                throw new IllegalArgumentException("No participant with id " + participantId);
                            }
                            var ap = new ActivityParticipant();
                            ap.getActivityParticipantKey().setParticipantId(participantId);
                            ap.setParticipant(participant);
                            ap.getActivityParticipantKey().setActivityId(activity.getId());
                            ap.setActivity(activity);
                            activity.getParticipants().add(ap);
                        });
                }
                event = this.eventRepository.save(event);
                return new UpdatedBooking(event, bookersToNotify);
            } finally {
                this.lock.unlock();
            }
        } else {
            throw new IllegalStateException("Impossible to acquire the lock to save bookings");
        }
    }
}
