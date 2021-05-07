package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
	private static String CSRF_HEADER = "X-CSRF-TOKEN";
	private static String SESSION_KEY = "JSESSIONID";
	
	@LocalServerPort
	private int port;
	
	private int currentRows;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		currentRows = countRowsInTable("events");
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("Upcoming events:"));
			assertThat(result.getResponseBody(), containsString("Previous events:"));
		});
	}
	
	@Test
	public void testGetEvent() {
		client.get().uri("/events/5").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("Code Jam 2022"));
				});
	}
	
	@Test
	public void testGetEventsByName() {
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("searchName", "code");

		
		client.get().uri("/events/search?searchName=code").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("Code Jam 2022"));
				});
	}
	
	@Test
	public void testGetAddEventPageNoUser() {
		client.get().uri("/events/add").accept(MediaType.TEXT_HTML)
				.exchange().expectStatus().isOk().expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
				.expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("Not signed in"));
				});
	}
	
	@Test
	public void testGetAddEventPageWithUser() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add").accept(MediaType.TEXT_HTML)
				.exchange().expectStatus().isOk().expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
				.expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("Event name:"));
					assertThat(result.getResponseBody(), containsString("Event date:"));
					assertThat(result.getResponseBody(), containsString("Event description:"));
				});
	}
	
	@Test
	public void testDeleteEvent() {
		String[] tokens = login();
		
		client.delete().uri("/events/5").accept(MediaType.TEXT_HTML).header(CSRF_HEADER,  tokens[0]).
				cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound();
		
		assertThat(currentRows - 1, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void testPostAddEventNoUser() {

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "test name"); 
	   	form.add("date", "2100-10-10");
	    form.add("time", "19:00");
	   	form.add("venue", "1");
	   	form.add("desc", "test description");

	   	client.post().uri("/events/add").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
	   			.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
	   			.value("Location", endsWith("/sign-in"));

	   	assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void testPostAddEvent() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "test name"); 
	   	form.add("date", "2100-10-10");
	    form.add("time", "19:00");
	   	form.add("venue", "1");
	   	form.add("desc", "test description");

 		client.post().uri("/events/add").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));

 		assertThat(currentRows + 1, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void testPostAddEventNoData() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);

 		client.post().uri("/events/add").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().isBadRequest();

 		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void testPostAddEventBadData() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "test name"); 
	   	form.add("date", "-1");
	    form.add("time", "19:00");
	   	form.add("venue", "-1");
	   	form.add("desc", "test description");

 		client.post().uri("/events/add").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().isOk();

 		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void testPostUpdateEventNoUser() {

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("id", "5");
		form.add("name", "test name"); 
	   	form.add("date", "2100-10-10");
	    form.add("time", "19:00");
	   	form.add("venueId", "1");
	   	form.add("description", "test description");

	   	client.post().uri("/events/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
	   			.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
	   			.value("Location", endsWith("/sign-in"));
	}
	
	
	@Test
	public void testPostUpdateEvent() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "5");
		form.add("name", "test name"); 
	   	form.add("date", "2100-10-10");
	    form.add("time", "19:00");
	   	form.add("venueId", "1");
	   	form.add("description", "test description");

 		client.post().uri("/events/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events/5"));
	}
	
	
	@Test
	public void testPostUpdateEventNoData() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);

 		client.post().uri("/events/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().is5xxServerError();
	}
	
	@Test
	public void testPostUpdateEventBadData() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "5");
		form.add("name", "test name"); 
	   	form.add("date", "-1");
	    form.add("time", "19:00");
	   	form.add("venueId", "-1");
	   	form.add("description", "test description");

 		client.post().uri("/events/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().isBadRequest();
	}
	
	@Test
	public void testPostTweetNoUser() {

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("id", "5"); 
	   	form.add("tweet", "niiice");

	   	client.post().uri("/events/tweet").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
	   			.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
	   			.value("Location", endsWith("/sign-in"));
	}
	
	@Test
	public void testPostTweet() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "5"); 
	   	form.add("tweet", "niiice");

 		client.post().uri("/events/tweet").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events/5"));
	}
	
	@Test
	public void testPostTweetNoData() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);

 		client.post().uri("/events/tweet").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().isBadRequest();
	}
	
	@Test
	public void testPostTweetBadData() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "-1"); 
	   	form.add("tweet", "niiice");

 		client.post().uri("/events/tweet").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));
	}
	
	private String[] login() {
		String[] tokens = new String[2];

		EntityExchangeResult<String> result = client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get()
				.uri("/").accept(MediaType.TEXT_HTML).exchange().expectBody(String.class).returnResult();
		tokens[0] = getCsrfToken(result.getResponseBody());
		tokens[1] = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		return tokens;
	}

	private String getCsrfToken(String body) {
		Matcher matcher = CSRF.matcher(body);

		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}
	
}
