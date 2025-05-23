// SPDX-License-Identifier: Apache-2.0
package org.montrealjug.billetterie.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.montrealjug.billetterie.ui.Utils.toPresentationParticipants;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.montrealjug.billetterie.entity.Activity;
import org.montrealjug.billetterie.entity.ActivityParticipant;
import org.montrealjug.billetterie.entity.ActivityParticipantKey;
import org.montrealjug.billetterie.entity.Participant;

public class UtilsTest {

    @Test
    void toPresentationActivities_should_count_participants_and_waiting_participants() {
        // Arrange
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setTitle("Test Activity");
        activity.setDescription("Test Description");
        activity.setMaxParticipants(10);
        activity.setMaxWaitingQueue(5);
        activity.setStartTime(LocalDateTime.now());

        Set<ActivityParticipant> participants = new HashSet<>();

        // Add 10 regular participants
        for (int i = 0; i < 10; i++) {
            ActivityParticipant ap = new ActivityParticipant();

            // Set a unique participant ID for each participant
            ActivityParticipantKey key = new ActivityParticipantKey();
            key.setActivityId(activity.getId());
            key.setParticipantId(i + 1); // Unique ID for each participant
            ap.setActivityParticipantKey(key);
            Participant participant = new Participant();
            participant.setId(i + 1);
            ap.setParticipant(participant);
            ap.setActivity(activity);
            participants.add(ap);
        }

        // Add 2 waiting participants
        for (int i = 0; i < 2; i++) {
            ActivityParticipant ap = new ActivityParticipant();

            // Set a unique participant ID for each participant
            ActivityParticipantKey key = new ActivityParticipantKey();
            key.setActivityId(activity.getId());
            key.setParticipantId(i + 100); // Unique ID for each waiting participant
            ap.setActivityParticipantKey(key);
            Participant participant = new Participant();
            participant.setId(i + 100);
            ap.setParticipant(participant);
            ap.setActivity(activity);
            participants.add(ap);
        }

        activity.setParticipants(participants);

        Set<Activity> activities = new HashSet<>();
        activities.add(activity);

        // Act
        var result = Utils.toPresentationActivities(activities);

        // Assert
        assertThat(result).hasSize(1);
        var presentationActivity = result.getFirst();
        assertThat(presentationActivity.id()).isEqualTo(1L);
        assertThat(presentationActivity.title()).isEqualTo("Test Activity");
        assertThat(presentationActivity.description()).isEqualTo("<p>Test Description</p>\n");
        assertThat(presentationActivity.maxParticipants()).isEqualTo(10);
        assertThat(presentationActivity.maxWaitingQueue()).isEqualTo(5);
        assertThat(presentationActivity.participants().size()).isEqualTo(10);
        assertThat(presentationActivity.waitingParticipants().size()).isEqualTo(2);
        assertThat(presentationActivity.registrationStatus()).isEqualTo(Activity.RegistrationStatus.WAITING_LIST);
    }

    @Test
    void retrieveBaseUrlTest() {
        // Create a mock HttpServletRequest
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        // Set up the mock to return specific values
        var requestURL = new StringBuffer("http://localhost:8080/some/path");
        when(request.getRequestURL()).thenReturn(requestURL);
        when(request.getRequestURI()).thenReturn("/some/path");
        when(request.getContextPath()).thenReturn("");

        // Call the method and verify the result
        String baseUrl = Utils.retrieveBaseUrl(request);
        assertEquals("http://localhost:8080", baseUrl);

        // Test with a different URL and context path
        requestURL = new StringBuffer("https://example.com/some/path");
        when(request.getRequestURL()).thenReturn(requestURL);
        when(request.getRequestURI()).thenReturn("/some/path");
        when(request.getContextPath()).thenReturn("/app");

        baseUrl = Utils.retrieveBaseUrl(request);
        assertEquals("https://example.com/app", baseUrl);
    }

    @Test
    void toPresentationParticipants_should_return_a_mutable_list() {
        var activity = new Activity();
        activity.setId(1L);
        var firstParticipant = new Participant();
        firstParticipant.setId(1L);
        firstParticipant.setFirstName("First");
        firstParticipant.setLastName("Last");
        var ap = new ActivityParticipant();
        ap.setParticipant(firstParticipant);
        ap.setActivity(activity);
        activity.setParticipants(Set.of(ap));

        var presentationParticipants = toPresentationParticipants(activity);

        assertThat(presentationParticipants).hasSize(1);
        presentationParticipants.add(new PresentationParticipant(new Participant(), false));

        assertThat(presentationParticipants).hasSize(2);
    }
}
