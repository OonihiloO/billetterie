// SPDX-License-Identifier: Apache-2.0
package org.montrealjug.billetterie.ui;

import jakarta.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.montrealjug.billetterie.entity.Activity;
import org.montrealjug.billetterie.entity.ActivityParticipant;
import org.montrealjug.billetterie.entity.Booker;
import org.montrealjug.billetterie.entity.Event;

public class Utils {

    static List<PresentationActivity> toPresentationActivities(Set<Activity> activities) {
        return activities.stream().map((Activity activity) -> toPresentationActivity(activity, true)).toList();
    }

    static List<PresentationParticipant> toPresentationParticipants(Activity activity) {
        // we need to have a mutable List here, so we can't use `toList()`
        return activity.getParticipants().stream().map(Utils::toPresentationParticipant).collect(Collectors.toList());
    }

    private static PresentationParticipant toPresentationParticipant(ActivityParticipant activityParticipant) {
        var isWaiting = activityParticipant.getActivity().getWaitingParticipants().contains(activityParticipant);
        var participant = activityParticipant.getParticipant();
        var checkInTime = activityParticipant.getCheckInTime();
        return new PresentationParticipant(participant, isWaiting, checkInTime);
    }

    static PresentationActivity toPresentationActivity(Activity activity, boolean html) {
        return new PresentationActivity(
            activity.getId(),
            activity.getTitle(),
            html ? markdownToHtml(activity.getDescription()) : activity.getDescription(),
            activity.getMaxParticipants(),
            activity.getMaxWaitingQueue(),
            toPresentationParticipants(activity),
            activity.getStartTime().toLocalTime(),
            activity.getImagePath()
        );
    }

    static PresentationBooker toPresentationBooker(Booker booker) {
        return new PresentationBooker(
            booker.getFirstName(),
            booker.getLastName(),
            booker.getEmail(),
            booker.getEmailSignature()
        );
    }

    static PresentationEvent toPresentationEvent(Event event) {
        return new PresentationEvent(
            event.getId(),
            event.getTitle(),
            markdownToHtml(event.getDescription()),
            event.getDate(),
            toPresentationActivities(event.getActivities()),
            event.isActive(),
            event.getImagePath(),
            event.getLocation()
        );
    }

    static String retrieveBaseUrl(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        String requestURI = request.getRequestURI();

        return requestURL.substring(0, requestURL.length() - requestURI.length()) + request.getContextPath();
    }

    private static final Parser MD_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().softbreak("<br />").build();

    static String markdownToHtml(String markdown) {
        Node document = MD_PARSER.parse(markdown);
        return HTML_RENDERER.render(document);
    }

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
}
