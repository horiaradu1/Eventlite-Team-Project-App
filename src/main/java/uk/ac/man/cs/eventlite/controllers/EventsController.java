package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String event_add_page(Model model) {
		model.addAttribute("venues", venueService.findAll());
		return "events/add";
	}
	
	@PostMapping(value = "/add")
	public String addEvent(	@RequestParam("name") String name, 
						   	@RequestParam("date") String date,
						   	@RequestParam("time") String time,
						   	@RequestParam("venue") String venueID,
						   	@RequestParam("desc") String desc,
						   	Model model) throws Exception {
		
		Event event = new Event();
		
		event.setName(name);
		
		try {
			event.setDate(LocalDate.parse(date));
		}catch (Exception e) {
			event.setDate(null);
		}

		try {
			event.setTime(LocalTime.parse(time));
		}catch (Exception e) {
			event.setTime(null);
		}
		
		event.setVenueId(Long.parseLong(venueID));
		event.setVenue(venueService.findOne(Long.parseLong(venueID)));
		
		event.setDescription(desc);
		
		String eventValidation = Event.validation(event);
		if (eventValidation.length() > 0) {
			model.addAttribute("error", eventValidation);
			model.addAttribute("venues", venueService.findAll());
			return "events/add";
		}
		
		eventService.save(event);
		return "redirect:/events";
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
