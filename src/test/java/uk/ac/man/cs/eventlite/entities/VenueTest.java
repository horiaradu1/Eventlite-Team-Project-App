package uk.ac.man.cs.eventlite.entities;

import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.MatcherAssert.assertThat;

public class VenueTest {
	@Test
	public void addVenueValidation() throws Exception {
		
		String name = "default";
		String street = "default";
		String postcode = "default"; // Validation is performed by maps API
		String capacity = "50";
		
		int capacityInt = 50;
		Venue ven = new Venue();
		String longString = new String(new char[300]).replace('\0', ' ');
		ven.setName(name);
		ven.setCapacity(capacityInt);
		ven.setStreet(street);
		ven.setPostcode(postcode);
		// Valid venue
		assertThat(0, equalTo(Venue.validation(ven).length()));
		// No name venue
		ven.setName("");
		assertThat(0, lessThan(Venue.validation(ven).length()));
		// long name venue
		ven.setName(longString);
		assertThat(0, lessThan(Venue.validation(ven).length()));
		ven.setName(name);
		// no street venue
		ven.setStreet("");
		assertThat(0, lessThan(Venue.validation(ven).length()));
		// long street venue
		ven.setStreet(longString);
		assertThat(0, lessThan(Venue.validation(ven).length()));
		ven.setStreet(street);
		// no postcode venue
		ven.setPostcode("");
		assertThat(0, lessThan(Venue.validation(ven).length()));
		// long postcode venue
		ven.setPostcode(longString);
		assertThat(0, lessThan(Venue.validation(ven).length()));
		ven.setPostcode(postcode);
		// Negative capacity venue
		ven.setCapacity(-1);
		assertThat(0, lessThan(Venue.validation(ven).length()));
	}
}
