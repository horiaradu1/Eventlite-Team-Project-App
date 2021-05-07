package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@SpringBootTest
@AutoConfigureMockMvc
public class VenuesControllerApiTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private VenueService venueService;
	
	@MockBean
	private EventService eventService;

	@Test
	public void getSingleVenue() throws Exception {
		when(venueService.findOne(0)).thenReturn(new Venue());

		mvc.perform(get("/api/venues/0").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("singleVenue")).andExpect(jsonPath("$.length()", equalTo(8)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/0")))
				.andExpect(jsonPath("$._links.venue.href", endsWith("/api/venues/0")))
				.andExpect(jsonPath("$._links.events.href", endsWith("/api/venues/0/events")))
				.andExpect(jsonPath("$._links.next3events.href", endsWith("/api/venues/0/next3events")));

		verify(venueService).findOne(0);
	}
	
	@Test
	public void getVenueEvents() throws Exception {
		Event ev1 = new Event(); 		Event ev2 = new Event();
		ev1.setId(0); 					ev2.setId(1);
		ev1.setName("Event X"); 		ev2.setName("Event Y");
		ev1.setDate(LocalDate.now()); 	ev2.setDate(LocalDate.now());
		ev1.setTime(LocalTime.now()); 	ev2.setTime(LocalTime.now());
		Venue v = new Venue();
		v.setId(0);
		
		ev1.setVenue(v);
		ev2.setVenue(v);
		when(eventService.findByVenueId(0)).thenReturn(new ArrayList<Event>() {{ add(ev1); add(ev2); }});

		mvc.perform(get("/api/venues/0/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("venueEventsCollection")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._embedded.length()", equalTo(1)))
				.andExpect(jsonPath("$._embedded.events[0].id", equalTo(0)))
				.andExpect(jsonPath("$._embedded.events[1].id", equalTo(1)))
				.andExpect(jsonPath("$._embedded.events[0].venueId", equalTo(0)))
				.andExpect(jsonPath("$._embedded.events[1].venueId", equalTo(0)))
				.andExpect(jsonPath("$._embedded.events[0].name").value("Event X"))
				.andExpect(jsonPath("$._embedded.events[1].name").value("Event Y"));

		verify(eventService).findByVenueId(0);
	}
	
	@Test
	public void getvenueNext3Events() throws Exception {
		Event ev1 = new Event(); 		Event ev2 = new Event();
		ev1.setId(0); 					ev2.setId(1);
		ev1.setName("Event X"); 		ev2.setName("Event Y");
		ev1.setDate(LocalDate.now()); 	ev2.setDate(LocalDate.now());
		ev1.setTime(LocalTime.now()); 	ev2.setTime(LocalTime.now());
		Venue v = new Venue();
		v.setId(0);
		
		ev1.setVenue(v);
		ev2.setVenue(v);
		when(venueService.findOne(0)).thenReturn(v);
		when(eventService.findUpcoming()).thenReturn(new ArrayList<Event>() {{ add(ev1); add(ev2); }});

		mvc.perform(get("/api/venues/0/next3events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("venueNext3Events")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._embedded.length()", equalTo(1)))
				.andExpect(jsonPath("$._embedded.events[0].id", equalTo(0)))
				.andExpect(jsonPath("$._embedded.events[1].id", equalTo(1)))
				.andExpect(jsonPath("$._embedded.events[0].venueId", equalTo(0)))
				.andExpect(jsonPath("$._embedded.events[1].venueId", equalTo(0)))
				.andExpect(jsonPath("$._embedded.events[0].name").value("Event X"))
				.andExpect(jsonPath("$._embedded.events[1].name").value("Event Y"));

		verify(venueService).findOne(0);
		verify(eventService).findUpcoming();
	}
}
