package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

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
		model.addAttribute("venues", getAllVenues());
		return "events/show";
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String updateEvent(@RequestParam("newid") String id, @RequestParam("newname") String name, 
			@RequestParam("newdate") String date, @RequestParam("newtime") String time, 
			@RequestParam("newvenue") String venueid, @RequestParam("newdesc") String description) {
	
		// Checking if any fields (except description or time) were left blank
		if (id == null || name == null || date == null) {
			return "redirect:/events";
		}
		
		// Set the event attributes
		Event event = new Event();
		event.setId(Long.parseLong(id));
		event.setName(name);
		event.setVenueId(Long.parseLong(venueid));
		event.setDate(LocalDate.parse(date));
		event.setTime(LocalTime.parse(time));
		event.setDescription(description.stripLeading());

		eventService.save(event);
		return "redirect:/events";
	}
	
	// Used to get an iterable of all venues without accessing venue service
	private Iterable<Venue> getAllVenues(){
		// Getting all events
		Iterable<Event> events = eventService.findAll();
		ArrayList<Venue> venues = new ArrayList<Venue>();
		// Finding each unique venue from the list of events
		for (Event e : events) {
			Venue v = e.getVenue();
			if(!venues.contains(v)) {
				venues.add(v);
			}
		}
		Iterable<Venue> venueIterable = venues;
		return venueIterable;
	}
}
