package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerTest {

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
	public void updateVenue() throws Exception {
		ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);
		MultiValueMap<String, String> values = new LinkedMultiValueMap<String, String>();
		values.add("id", "1");
		values.add("name", "Test");
		values.add("street", "Oxford Road");
		values.add("postcode", "20");
		values.add("capacity", "5");
		
		mvc.perform(post("/venues/update").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(values)
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
				.andExpect(view().name("redirect:/venues/1")).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("updateVenue")).andExpect(flash().attributeExists("ok_message"));

		verify(venueService).save(arg.capture());
		assertThat("Test", equalTo(arg.getValue().getName()));
	}
	
	@Test
	public void updateInvalidVenue() throws Exception {
		MultiValueMap<String, String> values = new LinkedMultiValueMap<String, String>();
		values.add("id", "1");
		values.add("name", "AaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaa\"\n" + 
				"aaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaAaAaaaaaaaaaaaaaaaaaaaaaaa\"\n" + 
				"AaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaa\"\n" + 
				"aaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		values.add("street", "Oxford Road");
		values.add("postcode", "20");
		values.add("capacity", "5");
		
		mvc.perform(post("/venues/update").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(values)
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
				.andExpect(view().name("redirect:/venues/1")).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("updateVenue")).andExpect(flash().attributeExists("bad_message"));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void deleteVenue() throws Exception {
		mvc.perform(delete("/venues/0").with(csrf()).with(user("Rob").roles(Security.ADMIN_ROLE))
			.accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
			.andExpect(view().name("redirect:/venues")).andExpect(handler().methodName("deleteVenue"));
		
		verify(venueService).deleteById(0);
	}
	
	@Test
	public void deleteVenueThatHasAnEvent() throws Exception {
		List<Event> list = new ArrayList<>();
		list.add(event);
		Iterable<Event> iterator = (Iterable<Event>)list;
		when(this.eventService.findByVenueId(0)).thenReturn(iterator);
		
		mvc.perform(delete("/venues/0").with(csrf()).with(user("Rob").roles(Security.ADMIN_ROLE))
			.accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
			.andExpect(view().name("redirect:/venues/0")).andExpect(handler().methodName("deleteVenue"));
		
		verify(venueService, never()).deleteById(0);
	}
	
	@Test
	public void addVenueValidation() throws Exception {
		
		String name = "default";
		String street = "default";
		String postcode = "default"; // Validation is performed by maps API
		String capacity = "50";
		
		// Get to venue page
		mvc.perform(get("/venues/add")
			.accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk())
			.andExpect(view().name("venues/add"));
		
		// Valid Venue
		mvc.perform(post("/venues/add")
			.with(user("Rob").roles(Security.ADMIN_ROLE))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.accept(MediaType.TEXT_HTML)
			.param("name", name)
			.param("street", street)
			.param("postcode", postcode)
			.param("capacity", capacity)
			.with(csrf()))
			.andExpect(status().isFound())
			.andExpect(model().hasNoErrors())
			.andExpect(handler().methodName("addVenue"))
			.andExpect(view().name("redirect:/venues"));
		
		// Non-integer capacity Venue
		mvc.perform(post("/venues/add")
			.with(user("Rob").roles(Security.ADMIN_ROLE))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.accept(MediaType.TEXT_HTML)
			.param("name", name)
			.param("street", street)
			.param("postcode", postcode)
			.param("capacity", "a")
			.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(model().hasNoErrors())
			.andExpect(handler().methodName("addVenue"))
			.andExpect(view().name("venues/add"));
		
		//  Venue validation failing event
		mvc.perform(post("/venues/add")
			.with(user("Rob").roles(Security.ADMIN_ROLE))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.accept(MediaType.TEXT_HTML)
			.param("name", "")
			.param("street", street)
			.param("postcode", postcode)
			.param("capacity", capacity)
			.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(model().hasNoErrors())
			.andExpect(handler().methodName("addVenue"))
			.andExpect(view().name("venues/add"));
		
		// Venue validation is tested in venue tests
	}
}
