package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;

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
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import uk.ac.man.cs.eventlite.EventLite;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
	private static String CSRF_HEADER = "X-CSRF-TOKEN";
	private static String SESSION_KEY = "JSESSIONID";
	
	@LocalServerPort
	private int port;

	private WebTestClient client;
	private int currentRows;
	
	@BeforeEach
	public void setup() {
		currentRows = countRowsInTable("venues");
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
	public void testGetAddNoAdmin() {
		client.get().uri("/venues/add").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
									.expectBody(String.class).consumeWith(result -> {
									assertThat(result.getResponseBody(), not(containsString("Enter venue name here")));
									assertThat(result.getResponseBody(), containsString("Not signed in"));
									});
	}
	
	@Test
	public void testPostAddNoUser() {

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "test name"); 
	   	form.add("street", "test street");
	    form.add("postcode", "test postcode");
	   	form.add("capacity", "10");

	   	client.post().uri("/venues/add").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
	   			.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
	   			.value("Location", endsWith("/sign-in"));

	   	assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void testPostAdd() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "test name"); 
	   	form.add("street", "test street");
	    form.add("postcode", "test postcode");
	   	form.add("capacity", "10");

 		client.post().uri("/venues/add").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/venues"));

 		assertThat(currentRows + 1, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void testPostAddNoData() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);

 		client.post().uri("/venues/add").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().isBadRequest();

 		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void testPostAddBadData() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "AaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaa\"\n" + 
				"aaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaAaAaaaaaaaaaaaaaaaaaaaaaaa\"\n" + 
				"AaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaa\"\n" + 
				"aaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"); 
	   	form.add("street", "test street");
	    form.add("postcode", "test postcode");
	   	form.add("capacity", "10");

 		client.post().uri("/venues/add").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().isOk();

 		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void testPostUpdateNoUser() {

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "test name"); 
	   	form.add("street", "test street");
	    form.add("postcode", "test postcode");
	   	form.add("capacity", "10");

	   	client.post().uri("/venues/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
	   			.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
	   			.value("Location", endsWith("/sign-in"));
	}
	
	
	@Test
	public void testPostUpdate() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("id", "1"); 
		form.add("name", "test name"); 
	   	form.add("street", "test street");
	    form.add("postcode", "test postcode");
	   	form.add("capacity", "10");

 		client.post().uri("/venues/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/venues/1"));
	}
	
	
	@Test
	public void testPostUpdateNoData() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);

 		client.post().uri("/venues/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().is4xxClientError();
	}
	
	@Test
	public void testPostUpdateBadData() {
		String[] tokens = login();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("name", "AaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaa\"\n" + 
				"aaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaAaAaaaaaaaaaaaaaaaaaaaaaaa\"\n" + 
				"AaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaAaaaaaaaaaaaaaaaa\"\n" + 
				"aaaaaaaaAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"); 
	   	form.add("street", "test street");
	    form.add("postcode", "test postcode");
	   	form.add("capacity", "10");

 		client.post().uri("/venues/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
 				.bodyValue(form).cookies(cookies -> {
 					cookies.add(SESSION_KEY, tokens[1]);
 				}).exchange().expectStatus().isBadRequest();
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
