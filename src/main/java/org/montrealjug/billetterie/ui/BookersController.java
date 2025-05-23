// SPDX-License-Identifier: Apache-2.0
package org.montrealjug.billetterie.ui;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.montrealjug.billetterie.entity.Booker;
import org.montrealjug.billetterie.exception.EntityNotFoundException;
import org.montrealjug.billetterie.exception.RedirectableNotFoundException;
import org.montrealjug.billetterie.repository.BookerRepository;
import org.montrealjug.billetterie.service.SignatureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("admin/bookers")
public class BookersController {

    private final BookerRepository bookerRepository;
    private final SignatureService signatureService;

    public BookersController(BookerRepository bookerRepository, SignatureService signatureService) {
        this.bookerRepository = bookerRepository;
        this.signatureService = signatureService;
    }

    @GetMapping("")
    public String bookers(Model model) {
        Iterable<Booker> bookers = bookerRepository.findAll();

        List<PresentationBooker> presentationBookers = StreamSupport
            .stream(bookers.spliterator(), false)
            .map(Utils::toPresentationBooker)
            .toList();

        model.addAttribute("bookerList", presentationBookers);

        return "bookers-list";
    }

    @PostMapping("")
    public ResponseEntity<Void> addBooker(@Valid PresentationBooker booker, Model model) throws Exception {
        Booker bookerEntity = new Booker();

        String email = booker.email().toLowerCase();

        if (bookerRepository.existsById(email)) {
            throw new RuntimeException("User with this email already exists");
        }

        bookerEntity.setEmail(email);
        bookerEntity.setFirstName(booker.firstName());
        bookerEntity.setLastName(booker.lastName());
        bookerEntity.setValidationTime(Instant.now());
        bookerEntity.setEmailSignature(signatureService.signAndTrim(email));

        bookerRepository.save(bookerEntity);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/admin/bookers")).build();
    }

    @GetMapping("add-booker")
    public String showAddBookerPage() {
        return "bookers-create-update";
    }

    @GetMapping("{emailSignature}")
    public String showUpdateBookerPage(Model model, @PathVariable String emailSignature) {
        this.bookerRepository.findByEmailSignature(emailSignature)
            .map(Utils::toPresentationBooker)
            .ifPresentOrElse(
                booker -> model.addAttribute("booker", booker),
                () -> {
                    throw new EntityNotFoundException("Booker with emailSignature " + emailSignature + " not found");
                }
            );
        return "bookers-create-update";
    }

    @PostMapping("{emailSignature}")
    public ResponseEntity<Void> updateBooker(
        Model model,
        @PathVariable String emailSignature,
        @Valid PresentationBooker presentationBooker
    ) {
        Optional<Booker> optionalBooker = bookerRepository.findByEmailSignature(emailSignature);

        if (optionalBooker.isEmpty()) {
            throw new RedirectableNotFoundException(
                "Booker with emailSignature " + emailSignature + " not found",
                "/admin/bookers/" + emailSignature
            );
        }

        Booker booker = optionalBooker.get();

        booker.setFirstName(presentationBooker.firstName());
        booker.setLastName(presentationBooker.lastName());

        //         TODO allow changing user's email address (currently unable since email is the
        // primary key
        //         of Booker)

        //        String updatedEmail = presentationBooker.email();
        //        if (!email.equals(updatedEmail)) {
        //            if (bookerRepository.existsById(updatedEmail)) {
        //                throw new RuntimeException("User with this email already exists");
        //            }
        //
        //            booker.setEmail(updatedEmail);
        //            booker.setEmailSignature(signatureService.signAndTrim(updatedEmail));
        //            booker.setValidationTime(Instant.now());
        //        }

        bookerRepository.save(booker);

        // return to the new email as url path variable if changed
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/admin/bookers")).build();
    }
}
