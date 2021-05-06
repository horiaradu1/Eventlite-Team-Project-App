package uk.ac.man.cs.eventlite.entities;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.MatcherAssert.assertThat;

public class VenueTest {
	private static Venue ven;
	private static String street;
	private static String postcode;
	
	@BeforeAll
	public static void createDefaultVenue() {
		street = "Kilburn Building University of Manchester, Oxford Rd, Manchester"; 
		postcode = "M13 9PL"; 
		
		ven = new Venue();
		ven.setStreet(street);
		ven.setPostcode(postcode);
	}

	@BeforeEach
	public void restoreDefaultVenue() {
		String name = "default";
		int capacityInt = 50;
		ven.setName(name);
		ven.setCapacity(capacityInt);
	}

	@Test
	public void validVenue() throws Exception {
		// Valid venue
		assertThat("", equalTo(Venue.validation(ven)));
	}

	@Test
	public void noNameVenue() throws Exception {
		// No name venue
		ven.setName("");
		assertThat(0, lessThan(Venue.validation(ven).length()));
	}

	@Test
	public void longNameVenue() throws Exception {
		// long name venue
		String longString = new String(new char[300]).replace('\0', ' ');
		ven.setName(longString);
		assertThat(0, lessThan(Venue.validation(ven).length()));
	}

	@Test
	public void noStreetVenue() throws Exception {
		// no street venue
		ven.setStreet("");
		assertThat(0, lessThan(Venue.validation(ven).length()));
		ven.setStreet(street);
	}

	@Test
	public void longStreetVenue() throws Exception {
		// long street venue
		String longString = new String(new char[300]).replace('\0', ' ');
		ven.setStreet(longString);
		assertThat(0, lessThan(Venue.validation(ven).length()));
		ven.setStreet(street);
	}

	@Test
	public void noPostcodeVenue() throws Exception {
		// no postcode venue
		ven.setPostcode("");
		assertThat(0, lessThan(Venue.validation(ven).length()));
		ven.setPostcode(postcode);
	}

	@Test
	public void longPostcodeVenue() throws Exception {
		// long postcode venue
		String longString = new String(new char[300]).replace('\0', ' ');
		ven.setPostcode(longString);
		assertThat(0, lessThan(Venue.validation(ven).length()));
		ven.setPostcode(postcode);
	}

	@Test
	public void negativeCapacityVenue() throws Exception {
		// Negative capacity venue
		ven.setCapacity(-1);
		assertThat(0, lessThan(Venue.validation(ven).length()));
	}
	
	@Test
	public void verifyGeocode() throws Exception {
		ven.setStreet(street);
		ven.setPostcode(postcode);
		assertThat(ven.getLatitude(), equalTo(53.467524));
		assertThat(ven.getLongitude(), equalTo(-2.233915));
	}
	
	@Test
	public void invalidAddressGeocode() throws Exception {
		ven.setStreet("");
		ven.setPostcode("");
		assertThat(ven.getLatitude(), equalTo(0.0)); //North pole
		assertThat(ven.getLongitude(), equalTo(0.0)); //North pole
	}
	
	
}
