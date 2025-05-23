// SPDX-License-Identifier: Apache-2.0
package org.montrealjug.billetterie.ui;

import org.montrealjug.billetterie.repository.EventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final EventRepository eventRepository;

    public IndexController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        this.eventRepository.findByActiveIsTrue()
            .map(Utils::toPresentationEvent)
            .ifPresent(event -> model.addAttribute("event", event));

        return "view/booker-index";
    }
}
