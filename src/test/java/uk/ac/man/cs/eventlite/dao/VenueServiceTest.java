package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class VenueServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private VenueService venueService;
	

	// This class is here as a starter for testing any custom methods within the
	// VenueService. Note: It is currently @Disabled!
	@Test
	public void findAllAlphabeticallyTest() {
		Venue venA = new Venue(); venA.setName("Venue A");
		Venue venB = new Venue(); venB.setName("Venue B");
		Venue venC = new Venue(); venC.setName("Venue C");

		// Insert venues in arbitrary order
		venueService.save(venB);
		venueService.save(venC);
		venueService.save(venA);
		
		assertTrue(venueService.count() >= 3);

		String previous = "";
		for (Venue ven : venueService.findAll()) {
			assertTrue(previous.compareTo(ven.getName()) <= 0);
			previous = ven.getName();
		}
	}
	
	@Test
	public void findByNameTest() {
		Venue venA = new Venue(); venA.setName("Venue A");
		Venue venB = new Venue(); venB.setName("Venue B");
		Venue venC = new Venue(); venC.setName("Venue C");

		// Insert venues in arbitrary order
		venueService.save(venB);
		venueService.save(venC);
		venueService.save(venA);
		
		int count = 0;
		for (Venue ven : venueService.findByName("a")) 
			count++;
		assertTrue(count == 1);
		
		count = 0;
		for (Venue ven : venueService.findByName("venue")) 
			count++;
		assertTrue(count == 3);
	}
	
	@Test
	public void findByNameAlphabeticallyTest() {
		Venue venA = new Venue(); venA.setName("Venue A");
		Venue venB = new Venue(); venB.setName("Venue B");
		Venue venC = new Venue(); venC.setName("Venue C");

		// Insert venues in arbitrary order
		venueService.save(venB);
		venueService.save(venC);
		venueService.save(venA);

		String previous = "";
		boolean check = true;
		for (Venue ven : venueService.findByName("venue")) {
			check = check && (previous.compareTo(ven.getName()) <= 0);
			previous = ven.getName();
		}
		assertTrue(check);
	}
	
	@Test
	public void findByNameNoResultTest() {
		int count = 0;
		for (Venue ven : venueService.findByName("not a venue")) 
			count++;
		assertTrue(count == 0);
	}
	
}
