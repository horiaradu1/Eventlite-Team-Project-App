package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/", produces = { MediaType.TEXT_HTML_VALUE })
public class HomePageController {
	
	@Autowired
	private EventService eventService;
	@Autowired
	private VenueService venueService;
	
	@GetMapping
	public String getAllVenues(Model model) {
		
		model.addAttribute("events", eventService.findUpcoming(3));
		model.addAttribute("venues", venueService.findTopVenues(3));
		model.addAttribute("numberOfEvents", venueService.findNumberEvents(3));
		
		return "home/index";
	}
	

}
