// SPDX-License-Identifier: Apache-2.0
package org.montrealjug.billetterie.ui;

import static org.montrealjug.billetterie.ui.BookerController.BookerFormType.CHECK;
import static org.montrealjug.billetterie.ui.BookerController.BookerFormType.CREATE;
import static org.montrealjug.billetterie.ui.Utils.retrieveBaseUrl;
import static org.montrealjug.billetterie.ui.Utils.toPresentationEvent;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.montrealjug.billetterie.email.EmailModel.Email;
import org.montrealjug.billetterie.email.EmailService;
import org.montrealjug.billetterie.entity.Activity.RegistrationStatus;
import org.montrealjug.billetterie.entity.Booker;
import org.montrealjug.billetterie.entity.Participant;
import org.montrealjug.billetterie.repository.BookerRepository;
import org.montrealjug.billetterie.repository.EventRepository;
import org.montrealjug.billetterie.service.BookingService;
import org.montrealjug.billetterie.service.QrCodeService;
import org.montrealjug.billetterie.service.SignatureService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bookers")
public class BookerController {

    private final BookerRepository bookerRepository;
    private final EventRepository eventRepository;
    private final EmailService emailService;
    private final SignatureService signatureService;
    private final QrCodeService qrCodeService;
    private final BookingService bookingService;

    public BookerController(
        BookerRepository bookerRepository,
        EventRepository eventRepository,
        EmailService emailService,
        SignatureService signatureService,
        QrCodeService qrCodeService,
        BookingService bookingService
    ) {
        this.bookerRepository = bookerRepository;
        this.eventRepository = eventRepository;
        this.emailService = emailService;
        this.signatureService = signatureService;
        this.qrCodeService = qrCodeService;
        this.bookingService = bookingService;
    }

    public enum BookerFormType {
        CHECK,
        CREATE,
    }

    @GetMapping(value = "/registration", produces = MediaType.TEXT_HTML_VALUE)
    public String registration(Model model) {
        model.addAttribute("type", CHECK);
        return "view/booker-registration-form";
    }

    @PostMapping(value = "/registration", produces = MediaType.TEXT_HTML_VALUE)
    public String registrationForm(
        @RequestBody MultiValueMap<String, String> body,
        Model model,
        HttpServletRequest request
    ) {
        var baseUrl = retrieveBaseUrl(request);
        var type = BookerFormType.valueOf(body.getFirst("type"));
        var email = body.getFirst("email");
        if (email == null) {
            throw new IllegalArgumentException("Missing email");
        }
        try {
            switch (type) {
                case CHECK -> {
                    var booker = bookerRepository.findById(email).orElse(null);
                    if (booker == null) {
                        // email is not known, need to create a new Booker
                        model.addAttribute("type", CREATE);
                        model.addAttribute("email", email);
                    } else {
                        final Email bookerEmail;
                        if (booker.getValidationTime() != null) {
                            // email is known, and confirm, send returning_booker email
                            bookerEmail = Email.returningBooker(booker, baseUrl);
                            // add the confirmation message
                            model.addAttribute(
                                "confirmationMessage",
                                "We've sent you an email to finish the registration, please check your inbox and click on the registration link to register your kids to the activities"
                            );
                        } else {
                            // email is known, but not confirmed, send after_registration email again
                            bookerEmail = Email.afterRegistration(booker, baseUrl);
                            // add the confirmation message
                            model.addAttribute(
                                "confirmationMessage",
                                "We've sent you an email to finish the registration, please check your inbox and click on the registration link to register your kids to the activities"
                            );
                        }
                        emailService.sendEmail(bookerEmail);
                    }
                }
                case CREATE -> {
                    if (!email.equals(body.getFirst("confirmEmail"))) {
                        throw new IllegalArgumentException("None matching emails");
                    }
                    var booker = new Booker();
                    booker.setEmail(email);
                    booker.setLastName(body.getFirst("lastName"));
                    booker.setFirstName(body.getFirst("firstName"));
                    var signature = signatureService.signAndTrim(email);
                    booker.setEmailSignature(signature);
                    bookerRepository.save(booker);
                    emailService.sendEmail(Email.afterRegistration(booker, baseUrl));
                    model.addAttribute(
                        "confirmationMessage",
                        "You have successfully registered your email address, please check your inbox and click on the verification link to register your kids to the activities"
                    );
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "view/booker-registration-form";
    }

    @GetMapping(value = "/{emailSignature}", produces = MediaType.TEXT_HTML_VALUE)
    public String bookerIndex(@PathVariable("emailSignature") String emailSignature, Model model) {
        var event = activeEvent();
        var booker = bookerForSignature(emailSignature);
        if (booker.getValidationTime() == null) {
            booker.setValidationTime(Instant.now());
            booker = bookerRepository.save(booker);
        }
        model.addAttribute("event", event);
        model.addAttribute("booker", booker);
        return "view/booker-index";
    }

    @PostMapping(value = "/{emailSignature}", produces = MediaType.TEXT_HTML_VALUE)
    public String updateRegistration(
        @PathVariable("emailSignature") String emailSignature,
        @RequestBody MultiValueMap<String, String> body,
        Model model,
        HttpServletRequest request
    ) throws IOException, InterruptedException {
        var baseUrl = retrieveBaseUrl(request);
        var booker = bookerForSignature(emailSignature);
        var activityParticipants = extractActivityParticipants("activity-participant", body);
        var event = eventRepository
            .findByActiveIsTrue()
            .orElseThrow(() -> new IllegalArgumentException("No active event"));
        var update = !Boolean.parseBoolean(body.getFirst("reset"));
        final PresentationEvent presentationEvent;
        if (update) {
            var updatedBookings = this.bookingService.saveBookings(booker, event, activityParticipants);
            presentationEvent = toPresentationEvent(updatedBookings.event());
            var bookerEmail = Email.afterParticipantsChanges(
                booker,
                presentationEvent,
                baseUrl,
                qrCodeService.generateQrCode(baseUrl + "/admin/bookings/" + booker.getEmailSignature())
            );
            emailService.sendEmail(bookerEmail);
            updatedBookings
                .bookersToNotify()
                .stream()
                .map(b -> {
                    try {
                        var qrCode = qrCodeService.generateQrCode(baseUrl + "/admin/bookings" + b.getEmailSignature());
                        return Email.afterParticipantsUpgrade(b, presentationEvent, baseUrl, qrCode);
                    } catch (IOException e) {
                        throw new IllegalStateException("failing to generate QrCode for upgrade email");
                    }
                })
                .forEach(emailService::sendEmail);
        } else {
            presentationEvent = toPresentationEvent(event);
        }
        model.addAttribute("event", presentationEvent);
        model.addAttribute("booker", booker);
        model.addAttribute("fullRefresh", true);
        return "view/booker-activities";
    }

    @PostMapping(value = "/{emailSignature}/{activityId}", produces = MediaType.TEXT_HTML_VALUE)
    public String updateParticipantsForActivity(
        @PathVariable("emailSignature") String emailSignature,
        @PathVariable("activityId") long activityId,
        @RequestBody MultiValueMap<String, String> body,
        Model model
    ) {
        var bookerContext = currentContext(emailSignature, activityId, body);
        if (bookerContext.withoutParticipant()) {
            return prepareNewParticipantView(bookerContext, false, model);
        }
        var edit = Boolean.parseBoolean(body.getFirst("edit"));
        var onChange = Boolean.parseBoolean(body.getFirst("on-change"));
        model.addAttribute("onChange", onChange);
        return prepareBookerActivityParticipantsView(bookerContext, edit || onChange, model);
    }

    static Map<Long, Set<Long>> extractActivityParticipants(String field, MultiValueMap<String, String> body) {
        var fieldValues = body.get(field);
        if (fieldValues != null) {
            return fieldValues
                .stream()
                .map(ap -> ap.split("-"))
                .collect(
                    Collectors.groupingBy(
                        ap -> Long.parseLong(ap[0]),
                        Collectors.mapping(ap -> Long.parseLong(ap[1]), Collectors.toSet())
                    )
                );
        } else {
            return Collections.emptyMap();
        }
    }

    static String prepareBookerActivityParticipantsView(BookerContext bookerContext, boolean edit, Model model) {
        model.addAttribute("event", bookerContext.event);
        model.addAttribute("booker", bookerContext.booker);
        if (edit) {
            model.addAttribute("activity", bookerContext.activity);
            model.addAttribute("willLoseSpot", bookerContext.willLoseSpot);
            return "view/booker-activity-participants";
        } else {
            return "view/booker-activities";
        }
    }

    public enum ParticipantFormType {
        CANCEL,
        SAVE,
        START,
    }

    @PostMapping(value = "/{emailSignature}/participants", produces = MediaType.TEXT_HTML_VALUE)
    public String newParticipant(
        @PathVariable("emailSignature") String emailSignature,
        @RequestBody MultiValueMap<String, String> body,
        Model model
    ) {
        var activityId = -1L;
        try {
            activityId = Long.parseLong(body.getFirst("activityId"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid activity id " + body.getFirst("activityId"));
        }
        var action = ParticipantFormType.valueOf(body.getFirst("action"));
        return switch (action) {
            case SAVE, CANCEL -> {
                if (action == ParticipantFormType.SAVE) {
                    var booker = bookerForSignature(emailSignature);
                    var firstName = body.getFirst("firstName");
                    if (firstName == null || firstName.isEmpty()) {
                        throw new IllegalArgumentException("First name is required");
                    }
                    var lastName = body.getFirst("lastName");
                    if (lastName == null || lastName.isEmpty()) {
                        throw new IllegalArgumentException("Last name is required");
                    }
                    var yearOfBirth = -1;
                    try {
                        yearOfBirth = Integer.parseInt(body.getFirst("yearOfBirth"));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid year of birth", e);
                    }
                    var minYearOfBirth = LocalDate.now().getYear() - 19;
                    var maxYearOfBirth = LocalDate.now().getYear() - 2;
                    if (yearOfBirth < minYearOfBirth || yearOfBirth > maxYearOfBirth) {
                        throw new IllegalArgumentException("%d is not a valid year of birth".formatted(yearOfBirth));
                    }
                    var participant = new Participant();
                    participant.setFirstName(firstName);
                    participant.setLastName(lastName);
                    participant.setYearOfBirth(yearOfBirth);
                    participant.setBooker(booker);
                    booker.getParticipants().add(participant);
                    bookerRepository.save(booker);
                }
                var bookerContext = currentContext(emailSignature, activityId, body);
                model.addAttribute("onChange", false);
                yield prepareBookerActivityParticipantsView(bookerContext, true, model);
            }
            default -> prepareNewParticipantView(currentContext(emailSignature, activityId, body), true, model);
        };
    }

    static String prepareNewParticipantView(BookerContext bookerContext, boolean backToActivity, Model model) {
        model.addAttribute("booker", bookerContext.booker);
        model.addAttribute("activity", bookerContext.activity);
        model.addAttribute("event", bookerContext.event);
        model.addAttribute("backToActivity", backToActivity);
        return "view/booker-participant";
    }

    Booker bookerForSignature(String emailSignature) {
        return bookerRepository
            .findByEmailSignature(emailSignature)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email"));
    }

    PresentationEvent activeEvent() {
        return eventRepository
            .findByActiveIsTrue()
            .map(Utils::toPresentationEvent)
            .orElseThrow(() -> new IllegalArgumentException("No active event"));
    }

    record BookerContext(PresentationEvent event, PresentationActivity activity, Booker booker, boolean willLoseSpot) {
        boolean withoutParticipant() {
            return this.booker.getParticipants().isEmpty();
        }
    }

    BookerContext currentContext(String emailSignature, long activityId, MultiValueMap<String, String> body) {
        var willLoseSpot = false;
        var event = activeEvent();
        var activity = event.activity(activityId);
        // store original RegistrationStatus as it is dynamically computed
        var previousActivityRegistrationStatus = activity.registrationStatus();
        var activityParticipants = extractActivityParticipants("activity-participant", body)
            .getOrDefault(activityId, Collections.emptySet());
        var alreadyBookedParticipant = extractActivityParticipants("booked-participant", body);
        var alreadyBookedParticipantIds = alreadyBookedParticipant
            .entrySet()
            .stream()
            .flatMap(e -> e.getValue().stream())
            .collect(Collectors.toSet());
        var booker = bookerForSignature(emailSignature);
        var bookerParticipantById = booker
            .getParticipants()
            .stream()
            .collect(Collectors.toMap(Participant::getId, Function.identity()));
        // update other activities with unsaved changes
        event
            .activities()
            .stream()
            .filter(a -> a.id() != activityId)
            .forEach(activityToUpdate -> {
                // get the unsaved Participant
                var unsavedBookedParticipants = alreadyBookedParticipant.getOrDefault(
                    activityToUpdate.id(),
                    Collections.emptySet()
                );
                // get the currently saved Participant
                var savedBookedParticipantIds = activityToUpdate
                    .allParticipants(booker)
                    .stream()
                    .map(PresentationParticipant::id)
                    .collect(Collectors.toSet());
                // compare current state and saved state and update current state if any differences found
                if (!unsavedBookedParticipants.equals(savedBookedParticipantIds)) {
                    activityToUpdate.allParticipants().removeIf(p -> !unsavedBookedParticipants.contains(p.id()));
                    unsavedBookedParticipants.removeAll(savedBookedParticipantIds);
                    unsavedBookedParticipants.forEach(participantId -> {
                        var isWaiting = activityToUpdate.registrationStatus() == RegistrationStatus.WAITING_LIST;
                        var participant = bookerParticipantById.get(participantId);
                        if (participant == null) {
                            throw new IllegalStateException("this is a bug");
                        }
                        activityToUpdate.allParticipants().add(new PresentationParticipant(participant, isWaiting));
                    });
                }
            });
        // ensure that no Participant is booked in more than one Activity
        if (activityParticipants.stream().anyMatch(alreadyBookedParticipantIds::contains)) {
            throw new IllegalArgumentException("already booked participant");
        }
        // check if Booker is removing a non-waiting Participant AND there's an open waiting list
        // to display a warning message
        var previousParticipants = activity
            .allParticipants(booker)
            .stream()
            .map(PresentationParticipant::id)
            .collect(Collectors.toSet());
        if (
            previousActivityRegistrationStatus != RegistrationStatus.OPEN &&
            !activityParticipants.containsAll(previousParticipants)
        ) {
            willLoseSpot = true;
        }
        // remove the removed Participant to current Activity
        activity.allParticipants().removeIf(p -> p.booker().equals(booker) && !activityParticipants.contains(p.id()));
        // add newly added Participant to current Activity
        booker
            .getParticipants()
            .stream()
            .filter(p -> activityParticipants.contains(p.getId()) && !previousParticipants.contains(p.getId()))
            .forEach(p -> {
                var isWaiting = activity.registrationStatus() == RegistrationStatus.WAITING_LIST;
                var participant = new PresentationParticipant(p, isWaiting);
                activity.allParticipants().add(participant);
            });
        return new BookerContext(event, activity, booker, willLoseSpot);
    }
}
