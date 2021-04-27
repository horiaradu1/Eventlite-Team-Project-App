package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
	
	@GetMapping("/search")
	public String getEventsByName(@ModelAttribute("searchName") String name, Model model) {
		if(name.equals("")) {
			model.addAttribute("venues", venueService.findAll());
			return "venues/index";
		}
		model.addAttribute("venues", venueService.findByName(name));
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

	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String event_add_page(Model model) {
		return "venues/add";
	}
	
	@PostMapping(value = "/add")
	public String addVenue(	@RequestParam("name") String name, 
						   	@RequestParam("street") String street,
						   	@RequestParam("postcode") String postcode,
						   	@RequestParam("capacity") String capacity,
						   	Model model) throws Exception {
		
		Venue venue = new Venue();
		
		venue.setName(name);
		venue.setStreet(street);
		venue.setPostcode(postcode);

		try {
			int cap = Integer.parseInt(capacity);
			venue.setCapacity(cap);
		}catch (Exception e) {
			venue.setCapacity(-1);
		}
		
		String venueValidation = Venue.validation(venue);
		if (venueValidation.length() > 0) {
			model.addAttribute("error", venueValidation);
			return "venues/add";
		}
		
		venueService.save(venue);
		return "redirect:/venues";
	}
	
	@PostMapping("/update")
	public String updateVenue(@RequestParam("id") Long id, 
						@RequestParam("name") String name, 
					   	@RequestParam("street") String street,
					   	@RequestParam("postcode") String postcode,
					   	@RequestParam("capacity") String capacity,
					   	Model model, RedirectAttributes redirectAttrs) 
			throws Exception  {
		
		Venue venue = new Venue();
		
		venue.setId(id);
		venue.setName(name);
		venue.setStreet(street);
		venue.setPostcode(postcode);
		
		try {
			int cap = Integer.parseInt(capacity);
			venue.setCapacity(cap);
		}catch (Exception e) {
			venue.setCapacity(-1);
		}
		
		String venueValidation = Venue.validation(venue);
		if (venueValidation.length() > 0) {
			model.addAttribute("error", venueValidation);
			String redirect = "redirect:/venues/" + venue.getId();
			redirectAttrs.addFlashAttribute("bad_message", "Invalid Venue");
			return redirect;
		}
		
		venueService.save(venue);
		redirectAttrs.addFlashAttribute("ok_message", "Venue updated.");
		String redirect = "redirect:/venues/" + venue.getId();
		return redirect;
	}
	
	@DeleteMapping("/{id}")
	public String deleteVenue(@PathVariable("id") long id, Model model, RedirectAttributes redirectAttrs) {
		// Check if events exist for the venue
		int size = 0;
		for(Event e : eventService.findByVenueId(id)) {
		   size++;
		   break;
		}
		if (size != 0) {
			System.out.println(eventService.findByVenueId(id));
			redirectAttrs.addFlashAttribute("bad_message", "Cannot delete a venue that has events.");
			String redirect = "redirect:/venues/" + id;
			return redirect;
		}
		venueService.deleteById(id);
		redirectAttrs.addFlashAttribute("ok_message", "Venue deleted.");
		return "redirect:/venues";
	}
}
