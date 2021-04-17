package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import javax.validation.Valid;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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

		model.addAttribute("upcoming", eventService.findUpcoming());
		model.addAttribute("previous", eventService.findPrevious());
		model.addAttribute("events", eventService.findAll());
		
		return "events/index";
	}
	
	@GetMapping("/search")
	public String getEventsByName(@ModelAttribute("searchName") String name, Model model) {
		if(name.equals("")) {
			model.addAttribute("upcoming", eventService.findUpcoming());
			model.addAttribute("previous", eventService.findPrevious());
			return "events/index";
		}
		model.addAttribute("upcoming", eventService.findByNameAfter(name));
		model.addAttribute("previous", eventService.findByNameBefore(name));
		return "events/index";
	}
	
	@GetMapping("/{id}")
	public String event(@PathVariable("id") long id, Model model) {
		Event event = eventService.findOne(id);
		if (event == null) return "redirect:/events"; // If event does not exist go to homepage
		model.addAttribute("id", event.getId());
		model.addAttribute("name", event.getName());
		model.addAttribute("date", event.getDateString());
		model.addAttribute("date2", event.getDate());
		model.addAttribute("location", event.getVenue());
		LocalTime time = event.getTime();
		if (time != null) {
			model.addAttribute("time", time);
		}
		model.addAttribute("venue", event.getVenue().getName());
		if (event.getDescription() != null) {
			model.addAttribute("description", event.getDescription());
		}
		model.addAttribute("venues", venueService.findAll());
		return "events/show";
	}
	
	
	@DeleteMapping("/{id}")
	public String deleteEvent(@PathVariable("id") long id, Model model) {
		eventService.deleteById(id);
		return "redirect:/events";
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

}
