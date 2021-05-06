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
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsControllerApi.class)
@Import(Security.class)
public class EventsControllerApiTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private EventService eventService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")));

		verify(eventService).findAll();
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss");
		
		Event e = new Event();
		e.setId(0);
		e.setName("Event");
		e.setDate(LocalDate.of(2021, 01, 01));
		e.setTime(LocalTime.of(01, 01, 01));
		Venue v = new Venue();
		e.setVenue(v);
		
		String eventDateTest = e.getDate().format(formatterDate);
		String eventTimeTest = e.getTime().format(formatterTime);
		
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(1)))
				.andExpect(jsonPath("$._embedded.events[0].name", equalTo(e.getName())))
				.andExpect(jsonPath("$._embedded.events[0].id", equalTo((int) e.getId())))
				.andExpect(jsonPath("$._embedded.events[0].date", equalTo(eventDateTest)))
				.andExpect(jsonPath("$._embedded.events[0].time", equalTo(eventTimeTest)))
				.andExpect(jsonPath("$._embedded.events[0].description", equalTo(e.getDescription())))
				.andExpect(jsonPath("$._embedded.events[0].venueId", equalTo((int) e.getVenueId())))
				.andExpect(jsonPath("$._embedded.events[0].venue.id", equalTo((int) e.getVenueId())));

		verify(eventService).findAll();
	}
}
