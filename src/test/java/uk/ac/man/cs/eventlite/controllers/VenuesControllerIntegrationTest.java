package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringContains.containsString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import uk.ac.man.cs.eventlite.EventLite;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;
	
	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
									.expectBody(String.class).consumeWith(result -> {
									assertThat(result.getResponseBody(), containsString("Kilburn, G23"));
									assertThat(result.getResponseBody(), containsString("80"));
									assertThat(result.getResponseBody(), containsString("M13 9PL"));
									assertThat(result.getResponseBody(), containsString("Oxford Rd"));
									assertThat(result.getResponseBody(), containsString("Online"));
									assertThat(result.getResponseBody(), containsString("100000"));
									assertThat(result.getResponseBody(), containsString("M14 4DU"));
									assertThat(result.getResponseBody(), containsString("Deramore St"));
									});
	}

	@Test
	public void testGetEventsByName() {
		client.get().uri("/venues/search?searchName=Online").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
									.expectBody(String.class).consumeWith(result -> {
									assertThat(result.getResponseBody(), containsString("Online"));
									assertThat(result.getResponseBody(), containsString("100000"));
									assertThat(result.getResponseBody(), containsString("M14 4DU"));
									assertThat(result.getResponseBody(), containsString("Deramore St"));
									assertThat(result.getResponseBody(), not(containsString("Kilburn, G23")));
									});
	}

	@Test
	public void testGetEventsByNameNoEvents() {
		client.get().uri("/venues/search?searchName=XXXXXXX").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
									.expectBody(String.class).consumeWith(result -> {
									assertThat(result.getResponseBody(), not(containsString("Online")));
									assertThat(result.getResponseBody(), not(containsString("Kilburn, G23")));
									});
	}

	@Test
	public void testGetEventsByNameNoName() {
		//Should return all events
		client.get().uri("/venues/search?searchName=").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
									.expectBody(String.class).consumeWith(result -> {
									assertThat(result.getResponseBody(), containsString("Deramore St"));
									assertThat(result.getResponseBody(), containsString("Online"));
									});
	}

	@Test
	public void testGetEventById() {
		client.get().uri("/venues/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
									.expectBody(String.class).consumeWith(result -> {
									assertThat(result.getResponseBody(), containsString("Kilburn, G23"));
									assertThat(result.getResponseBody(), containsString("80"));
									assertThat(result.getResponseBody(), containsString("M13 9PL"));
									assertThat(result.getResponseBody(), containsString("Oxford Rd"));
									});
	}

	@Test
	public void testGetEventByIdInvalid() {
		// Check for redirection
		client.get().uri("/venues/-1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testGetAddWithAdmin() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/venues/add").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
									.expectBody(String.class).consumeWith(result -> {
									assertThat(result.getResponseBody(), containsString("Enter venue name here"));
									assertThat(result.getResponseBody(), containsString("Signed in"));
									});
	}

	@Test
	public void testGetAddNoAdmin() {
		client.get().uri("/venues/add").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
									.expectBody(String.class).consumeWith(result -> {
									assertThat(result.getResponseBody(), not(containsString("Enter venue name here")));
									assertThat(result.getResponseBody(), containsString("Not signed in"));
									});
	}

}
