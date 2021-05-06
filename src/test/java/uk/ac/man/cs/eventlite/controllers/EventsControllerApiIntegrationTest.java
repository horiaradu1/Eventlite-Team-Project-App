package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;

import javassist.bytecode.Descriptor.Iterator;
import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerApiIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;
	
	@Autowired
	private EventService eventService;
	
	private Event event;
	
	private int counterEvents = 0;
	
	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/api").build();
		
		if(event == null) {
			Iterable<Event> events = eventService.findAll();
			java.util.Iterator<Event> iter = events.iterator();
			event = iter.next();
			if(counterEvents == 0) {
				for(Object i : events) {
					counterEvents++;
				}
			}
		}
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$._links.self.href").value(endsWith("/api/events"))
				.jsonPath("$._embedded.events.length()").value(equalTo(counterEvents));
	}
	
	@Test
	public void testGetEvent() {
		int venueIdTest = (int) event.getVenueId();
		DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String eventDateTest = event.getDate().format(formatterDate);
		DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss");
		String eventTimeTest = event.getTime().format(formatterTime);
		client.get().uri("/events/" + event.getId()).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$._links.length()").value(equalTo(3))
				.jsonPath("$._links.self.href").value(endsWith("/api/events/" + event.getId()))
				.jsonPath("$._links.event.href").value(endsWith("/api/events/" + event.getId()))
				.jsonPath("$._links.venue.href").value(endsWith("/api/events/" + event.getId() + "/venue"))
				.jsonPath("$.name").value(equalTo(event.getName()))
				.jsonPath("$.date").value(equalTo(eventDateTest))
				.jsonPath("$.time").value(equalTo(eventTimeTest))
				.jsonPath("$.venueId").value(equalTo(venueIdTest))
				.jsonPath("$.description").value(equalTo(event.getDescription()))
				.jsonPath("$.dateString").value(equalTo(event.getDateString()));
	}
	
	@Test
	public void testGetEventVenue() {
		int venueIdTest = (int) event.getVenueId();
		Venue venue = event.getVenue();
		client.get().uri("/events/" + event.getId() + "/venue").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$._links.length()").value(equalTo(2))
				.jsonPath("$._links.self.href").value(endsWith("/api/venues/" + venueIdTest))
				.jsonPath("$._links.venue.href").value(endsWith("/api/venues/" + venueIdTest))
				.jsonPath("$.name").value(equalTo(venue.getName()))
				.jsonPath("$.street").value(equalTo(venue.getStreet()))
				.jsonPath("$.postcode").value(equalTo(venue.getPostcode()))
				.jsonPath("$.capacity").value(equalTo(venue.getCapacity()))
				.jsonPath("$.latitude").value(equalTo(venue.getLatitude()))
				.jsonPath("$.longitude").value(equalTo(venue.getLongitude()));
	}
}
