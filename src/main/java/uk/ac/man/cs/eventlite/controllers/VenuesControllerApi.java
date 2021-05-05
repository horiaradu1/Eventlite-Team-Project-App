package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

	@Autowired
	private VenueService venueService;
	
	@Autowired
	private EventService eventService;

	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public EntityModel<Venue> singleVenue(@PathVariable long id) {
		Venue venue = venueService.findOne(id);
		
		Link selfLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).withSelfRel();
		Link venueLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).withRel("venue");
		Link eventsLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).slash("events").withRel("events");
		Link next3events = linkTo(VenuesControllerApi.class).slash(venue.getId()).slash("next3events").withRel("next3events");
		
		return EntityModel.of(venue, selfLink, venueLink, eventsLink, next3events);
	}
	
	@RequestMapping(value="/{id}/events", method=RequestMethod.GET)
	public CollectionModel<Event> venueEventsCollection(@PathVariable long id) {
		
		Iterable<Event> allVenuesForEvent = eventService.findByVenueId(id);
		return CollectionModel.of(allVenuesForEvent);
	}
	
	@RequestMapping(value="/{id}/next3events", method=RequestMethod.GET)
	public CollectionModel<Event> venueNext3Events(@PathVariable long id) {
		Venue venue = venueService.findOne(id);
		Iterable<Event> nextEvents = eventService.findUpcoming();
		
		ArrayList<Event> nextEventsForVenue = new ArrayList<Event>();
		
		Iterator<Event> iter = nextEvents.iterator();
		while(iter.hasNext()){
			Event event = iter.next();
			if(venue == event.getVenue()) {
				nextEventsForVenue.add(event);
				if(nextEventsForVenue.size() == 3) {
					return CollectionModel.of(nextEventsForVenue);
				}
			}
		}
		
		return CollectionModel.of(nextEventsForVenue);
	}
	


}
