package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
		return "events/show";
	}
	
	@GetMapping("/add")
	public String event_add_page(Model model) {
		return "events/add";
	}
	
	
//	@GetMapping("/add")
//	public String event_add(Model model) {
//		
//		ev.setId(47);
//		ev.setName("ONG 2018");
//		ev.setDescription("OGN Something");
//		eventService.save(ev);
//		return "events";
//	}
}
