package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
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

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import org.springframework.util.LinkedMultiValueMap;

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
		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		verify(eventService).findPrevious();
		verify(eventService).findUpcoming();
	}
	
	@Test
	public void checkCorrectLongLat() throws Exception {
		Venue ven = new Venue();
		ven.setName("Kilburn");
		ven.setCapacity(100);
		ven.setId(0);
		ven.setStreet("Strada Balta Albina nr. 9, Bucure??ti");
		ven.setPostcode("032622");
		
		double testLat = 44.40864436198193;
		double testLong = 26.210873520635612;
		
		assertTrue(Math.abs(testLong - ven.getLongitude()) < 0.001);
		assertTrue(Math.abs(testLat - ven.getLatitude()) < 0.001);
		
	}
	
	@Test
	public void checkMapBoxApi() throws Exception {
		Venue ven = new Venue();
		ven.setName("Kilburn");
		ven.setCapacity(100);
		ven.setId(0);
		
		//Empty address test
		ven.setStreet("");
		ven.setPostcode("");
		ven.addressGeocode();
		assertTrue( ven.getLatitude() == 0 && ven.getLongitude() == 0);
		
		
		//Invalid address test
		ven.setStreet("((((((");
		ven.setPostcode("((((((");
		ven.addressGeocode();
		assertTrue( ven.getLatitude() == 0 && ven.getLongitude() == 0);
	}
	//Could be improved
	@Test
	public void checkTweet() throws Exception {
		ConfigurationBuilder cb = new ConfigurationBuilder();
	    cb.setDebugEnabled(true)
	    .setOAuthConsumerKey("3GPfdMNYao6FSWlacVap2SOuU")
	    .setOAuthConsumerSecret("76IfrYzbkqjuNc5tHsPYcNuyElMV7vwXJSh6zWytPEFp4UC31c")
	    .setOAuthAccessToken("1381697881585950722-dYJk01rGhj8W296WaeInEM1lvrmcq2")
	    .setOAuthAccessTokenSecret("3gtEKzDtVUPdtL1476zY8tn8rlUpZMVru2P3GxDYw3LEQ");
	    TwitterFactory tf = new TwitterFactory(cb.build());
	    Twitter twitter = tf.getInstance();
	    
	    int check = 0;
	    //Test if tweet can be sent
	    try {
			twitter.updateStatus("test");
			check = 1;
			
		} catch (TwitterException e) {
			assertFalse(check == 1);
		}
	    assertTrue(check == 1);
	    
	    check = 0;
	    //Test if tweet can be retrieved
	    try {
	    	Status tweet = twitter.getUserTimeline().get(0);
	    	if (tweet.getText().equals("test")) 
	    		check = 1;
	    	twitter.destroyStatus(tweet.getId());
	    }
	    catch (TwitterException e) {
			assertFalse(check == 1);
		}
		assertTrue(check == 1);
		
	}
	
	@Test
	public void getEventValid() throws Exception {
		Venue ven = new Venue();
		ven.setName("Kilburn");
		ven.setCapacity(100);
		ven.setId(0);
		ven.setStreet("street");
		ven.setPostcode("postcode");
		
		Event ev = new Event();
		ev.setId(0);
		ev.setVenue(ven);
		ev.setDate(LocalDate.now());
		ev.setTime(LocalTime.now());
		ev.setName("GameJame");
		ev.setDescription("sss");

		when(this.eventService.findOne(0)).thenReturn(ev);
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
	public void deleteEvent() throws Exception {
		mvc.perform(delete("/events/0").with(csrf()).with(user("Rob").roles(Security.ADMIN_ROLE))
			.accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
			.andExpect(view().name("redirect:/events")).andExpect(handler().methodName("deleteEvent"));
		
		verify(eventService).deleteById(0);
	}

	@Test
	public void searchEvent() throws Exception{
		when(this.venue.getName()).thenReturn("Kilburn");
		when(this.event.getName()).thenReturn("ONG 2018");
		when(this.event.getDate()).thenReturn(LocalDate.now());
		when(this.event.getTime()).thenReturn(LocalTime.now());
		when(this.event.getVenue()).thenReturn(venue);
		when(this.event.getDescription()).thenReturn("Nothing");
		when(this.eventService.findByNameBefore("ONG")).thenReturn(Collections.<Event>singletonList(event));
		
		mvc.perform(get("/events/search?searchName=ONG").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
			.andExpect(view().name("events/index")).andExpect(handler().methodName("getEventsByName"));
		
		verify(eventService).findByNameBefore("ONG");
	}
	
	@Test
	public void updateEvent() throws Exception {
		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		MultiValueMap<String, String> values = new LinkedMultiValueMap<String, String>();
		values.add("id", "1");
		values.add("name", "Test");
		values.add("date", "2021-10-10");
		values.add("time", "");
		values.add("venueId", "1");
		values.add("description", "");
		
		mvc.perform(post("/events/update").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(values)
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
				.andExpect(view().name("redirect:/events/1")).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("updateEvent")).andExpect(flash().attributeExists("ok_message"));

		verify(eventService).save(arg.capture());
		assertThat("Test", equalTo(arg.getValue().getName()));
	}
	
	@Test
	public void updateInvalidEvent() throws Exception {
		MultiValueMap<String, String> values = new LinkedMultiValueMap<String, String>();
		values.add("id", "1");
		values.add("name", "AaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaa"
				+ "aaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaAaAaaaaaaaaaaaaaaaaaaaaaaa"
				+ "AaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaa"
				+ "aaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		values.add("date", "2021-10-10");
		values.add("time", "");
		values.add("venueId", "1");
		values.add("description", "");
		mvc.perform(post("/events/update").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(values)
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
				.andExpect(view().name("redirect:/events/1"))
				.andExpect(handler().methodName("updateEvent"));

		verify(eventService, never()).save(event);
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
