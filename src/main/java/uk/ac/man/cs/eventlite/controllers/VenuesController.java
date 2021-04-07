package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {
	
	@Autowired
	private EventService eventService;
	@Autowired
	private VenueService venueService;
	
	@GetMapping
	public String getAllVenues(Model model) {
		
		model.addAttribute("venues", venueService.findAll());
		
		return "venues/index";
	}
	
	@GetMapping("/{id}")
	public String venue(@PathVariable("id") long id, Model model) {
		Venue venue = venueService.findOne(id);
		if (venue == null) return "redirect:/venues";
		model.addAttribute("name", venue.getName());
		model.addAttribute("street", venue.getStreet());
		model.addAttribute("postcode", venue.getPostcode());
		model.addAttribute("capacity", venue.getCapacity());
		
		model.addAttribute("upcoming", eventService.findByVenueId(id));
		return "venues/show";
	}

}
