package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerApiIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;
	
	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/api").build();
	}
	
	@Test
	public void getVenuesTest() {
		client.get().uri("/venues").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
			.contentType(MediaType.APPLICATION_JSON).expectBody()
			.jsonPath("$.length()").value(equalTo(2))
			.jsonPath("$._links.self.href").value(endsWith("/api/venues"))
			.jsonPath("$._links.profile.href").value(endsWith("/api/profile/venues"))
			.jsonPath("$._links.search.href").value(endsWith("/api/venues/search"));	
	}

	@Test
	public void getSingleVenueTest() {
		client.get().uri("/venues/1").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$._links.self.href").value(endsWith("/api/venues/1"))
				.jsonPath("$._links.venue.href").value(endsWith("/api/venues/1"))
				.jsonPath("$._links.events.href").value(endsWith("/api/venues/1/events"))
				.jsonPath("$._links.next3events.href").value(endsWith("/api/venues/1/next3events"));
	}
	
	@Test
	public void getVenueEventsTest() {
		client.get().uri("/venues/3/events").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$._embedded.length()").value(equalTo(1))
				.jsonPath("$._embedded.events.length()").value(equalTo(2))
				.jsonPath("$._embedded.events[0].id").value(equalTo(5))
				.jsonPath("$._embedded.events[1].id").value(equalTo(11))
				.jsonPath("$._embedded.events[0].venueId").value(equalTo(3))
				.jsonPath("$._embedded.events[1].venueId").value(equalTo(3))
				.jsonPath("$._embedded.events[0].name").value(endsWith("ONI 2019"))
				.jsonPath("$._embedded.events[1].name").value(endsWith("ONM 2022"));
	}
	
	@Test
	public void getvenueNext3EventsTest() {
		client.get().uri("/venues/2/next3events").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$._embedded.length()").value(equalTo(1))
				.jsonPath("$._embedded.events.length()").value(equalTo(3))
				.jsonPath("$._embedded.events[0].id").value(equalTo(7))
				.jsonPath("$._embedded.events[1].id").value(equalTo(10))
				.jsonPath("$._embedded.events[2].id").value(equalTo(13))
				.jsonPath("$._embedded.events[0].venueId").value(equalTo(2))
				.jsonPath("$._embedded.events[1].venueId").value(equalTo(2))
				.jsonPath("$._embedded.events[2].venueId").value(equalTo(2))
				.jsonPath("$._embedded.events[0].name").value(endsWith("Code Jam 2022"))
				.jsonPath("$._embedded.events[1].name").value(endsWith("World Cup 2022"))
				.jsonPath("$._embedded.events[2].name").value(endsWith("World Cyber Games 2022"));
	}
}
