package uk.ac.man.cs.eventlite.controllers;


import java.time.LocalDate;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;


@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;
	@Autowired
	private VenueService venueService;

	@GetMapping
	public String getAllEvents(Model model) {

		model.addAttribute("events", eventService.findAll());

		return "events/index";
	}
	
	@GetMapping("/{id}")
	public String event(@PathVariable("id") long id, Model model) {
		Event event = eventService.findOne(id);
		if (event == null) return "redirect:/events"; // If event does not exist go to homepage
		model.addAttribute("name", event.getName());
		model.addAttribute("date", event.getDate());
		model.addAttribute("time", event.getTime());
		model.addAttribute("venue", event.getVenue().getName());
		model.addAttribute("description", event.getDescription());
		model.addAttribute("venues", venueService.findAll());
		return "events/show";
	}
	
	@PostMapping("/update")
	public String updateEvent(@RequestBody @Valid @ModelAttribute Event event, RedirectAttributes redirectAttrs) {
		// Validation
		String name = event.getName();
		String description = event.getDescription().trim();
		event.setDescription(description);
		if(!name.matches("[\\w*\\s*]*") || !description.matches("[\\w*\\s*]*")) {
			// Can only contain chars or spaces
			redirectAttrs.addFlashAttribute("bad_message", "Cannot use special characters!");
			String redirect = "redirect:/events/" + event.getId();
			return redirect;
		}
		// Name must be < 256 chars
		if(name.length() >= 256) {
			redirectAttrs.addFlashAttribute("bad_message", "Name must be less than 256 characters!");
			String redirect = "redirect:/events/" + event.getId();
			return redirect;
		}
		// Description must be < 500 chars
		if(description.length() >= 500) {
			redirectAttrs.addFlashAttribute("bad_message", "Description must be less than 500 characters!");
			String redirect = "redirect:/events/" + event.getId();
			return redirect;
		}
		if(!event.getDate().isAfter(LocalDate.now())) {
			// Date must be in the future
			redirectAttrs.addFlashAttribute("bad_message", "Date must be in the future!");
			String redirect = "redirect:/events/" + event.getId();
			return redirect;
		}
		// Saving the event
		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "Event updated.");
		String redirect = "redirect:/events/" + event.getId();
		return redirect;
	}
}
