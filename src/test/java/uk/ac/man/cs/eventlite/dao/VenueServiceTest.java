package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.servlet.MockMvc;

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
	
	@Autowired
	private EventService eventService;

	@BeforeEach
	public void insertVenues() {
		Venue venA = new Venue(); venA.setName("Venue A");
		Venue venB = new Venue(); venB.setName("Venue B");
		Venue venC = new Venue(); venC.setName("Venue C");

		// Insert venues in an arbitrary order
		venueService.save(venB);
		venueService.save(venC);
		venueService.save(venA);
	}

	// This class is here as a starter for testing any custom methods within the
	// VenueService. Note: It is currently @Disabled!
	@Test
	public void countTest() {
		int cntBefore = (int)venueService.count();
		Venue ven = new Venue();
		venueService.save(ven);
		assertTrue(cntBefore + 1 == venueService.count());
	}
	
	@Test
	public void findOneTestFound() {
		Venue ven = new Venue();
		venueService.save(ven);
		assertSame(ven, venueService.findOne(ven.getId()));
	}
	
	@Test
	public void findOneTestNotFound() {
		assertSame(null, venueService.findOne(-1));
	}
	
	@Test
	public void deleteByIdTest() {
		Venue ven = new Venue();
		venueService.save(ven);
		assertSame(ven, venueService.findOne(ven.getId()));
		venueService.deleteById(ven.getId());
		assertSame(null, venueService.findOne(-1));
	}
	
	@Test
	public void findAllAlphabeticallyTest() {
		String previous = "";
		for (Venue ven : venueService.findAll()) {
			assertTrue(previous.compareTo(ven.getName()) <= 0);
			previous = ven.getName();
		}
	}

	@Test
	public void findByNameTest() {
		int count = 0;
		for (Venue ven : venueService.findByName("ven")) 
			count++;
		assertTrue(count == 3);
		
		count = 0;
		for (Venue ven : venueService.findByName("venue")) 
			count++;
		assertTrue(count == 3);
	}

	@Test
	public void findByNameAlphabeticallyTest() {
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
	
	@Test
	public void findNumberEventsTest() {
		int count = (int)venueService.count();
		int previous = 1 << 30;
		for (Integer val : venueService.findNumberEvents(count)) {
			assertTrue(previous >= val);
			previous = val;
		}
	}
	
	@Test
	public void findTopVenuesTest() {
		Iterable<Venue> popularVenues = venueService.findTopVenues((int)venueService.count());

		int previous = 1 << 30;
		for (Venue venue : popularVenues) {
			int associatedEvents = 0;
			for (Event event : eventService.findAll())
				if (event.getVenue() == venue) associatedEvents++;
			assertTrue(previous >= associatedEvents);
			previous = associatedEvents;
		}
	}
}
