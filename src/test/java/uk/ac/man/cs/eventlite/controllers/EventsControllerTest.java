package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerTest {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		//verify(venueService).findAll();
		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

		//when(event.getVenue()).thenReturn(1L);
		when(event.getVenue()).thenReturn(venue);
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		//verify(venueService).findAll();
	}
	
	@Test
	public void getEventValid() throws Exception {
		when(this.venue.getName()).thenReturn("Kilburn");
		when(this.event.getName()).thenReturn("GameJame");
		when(this.event.getDate()).thenReturn(LocalDate.now());
		when(this.event.getTime()).thenReturn(LocalTime.now());
		when(this.event.getVenue()).thenReturn(venue);
		when(this.event.getDescription()).thenReturn("Nothing");
		when(this.eventService.findOne(0)).thenReturn(event);

		mvc.perform(get("/events/0").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
			.andExpect(view().name("events/show")).andExpect(handler().methodName("event"));
		
		verify(eventService).findOne(0);
	}
	
	@Test
	public void getEventInvalid() throws Exception {
		when(this.eventService.findOne(0)).thenReturn(null);
		mvc.perform(get("/events/0").accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
			.andExpect(view().name("redirect:/events")).andExpect(handler().methodName("event"));
		
		verify(eventService).findOne(0);
	}
	
	@Test
	public void addEventValidation() throws Exception {
		
		when(venueService.findOne(venue.getId())).thenReturn(venue);
		String venueId = Long.toString(venue.getId());
		
		LocalDate today = LocalDate.now();
		String futureDate = today.plusDays(7).toString();
		String pastDate = today.minusDays(7).toString();
		
		String longString = ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
							"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
							"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
							"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
							"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
							"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
							"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
							"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
							"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
							"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
							"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		
		// Get to event page
		mvc.perform(get("/events/add")
			.accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk())
			.andExpect(view().name("events/add"));
		
		// Valid Event
		mvc.perform(post("/events/add")
			.with(user("Rob").roles(Security.ADMIN_ROLE))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.accept(MediaType.TEXT_HTML)
			.param("name", "Event Name Test")
			.param("date", futureDate)
			.param("time", "12:12:12")
			.param("venue", venueId)
			.param("desc", "Event Description Test")
			.with(csrf()))
			.andExpect(status().isFound())
			.andExpect(model().hasNoErrors())
			.andExpect(handler().methodName("addEvent"))
			.andExpect(view().name("redirect:/events"));
		
		// No Name Event
		mvc.perform(post("/events/add")
			.with(user("Rob").roles(Security.ADMIN_ROLE))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.accept(MediaType.TEXT_HTML)
			.param("name", "")
			.param("date", futureDate)
			.param("time", "")
			.param("venue", venueId)
			.param("desc", "Event Description Test")
			.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(model().hasNoErrors())
			.andExpect(handler().methodName("addEvent"))
			.andExpect(view().name("events/add"));
		
		// Too long Name Event
		mvc.perform(post("/events/add")
			.with(user("Rob").roles(Security.ADMIN_ROLE))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.accept(MediaType.TEXT_HTML)
			.param("name", "Event Name Test" + longString)
			.param("date", futureDate)
			.param("time", "12:12:12")
			.param("venue", venueId)
			.param("desc", "Event Description Test")
			.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(model().hasNoErrors())
			.andExpect(handler().methodName("addEvent"))
			.andExpect(view().name("events/add"));
		
		// No Date Event
		mvc.perform(post("/events/add")
			.with(user("Rob").roles(Security.ADMIN_ROLE))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.accept(MediaType.TEXT_HTML)
			.param("name", "Event Name Test")
			.param("date", "")
			.param("time", "")
			.param("venue", venueId)
			.param("desc", "Event Description Test")
			.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(model().hasNoErrors())
			.andExpect(handler().methodName("addEvent"))
			.andExpect(view().name("events/add"));
		
		// Past Date Event
		mvc.perform(post("/events/add")
			.with(user("Rob").roles(Security.ADMIN_ROLE))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.accept(MediaType.TEXT_HTML)
			.param("name", "Event Name Test")
			.param("date", pastDate)
			.param("time", "12:12:12")
			.param("venue", venueId)
			.param("desc", "Event Description Test")
			.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(model().hasNoErrors())
			.andExpect(handler().methodName("addEvent"))
			.andExpect(view().name("events/add"));
	}
}
