package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.assertj.core.internal.Iterables;
import org.junit.jupiter.api.BeforeEach;
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
public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {
	static Event ev;
	
	@Autowired
	private EventService eventService;

	@BeforeEach
	public void insertEvent() {
		ev = new Event();
		ev.setDate(LocalDate.now());
		ev.setTime(LocalTime.now());
		ev.setName("ONG 2018");
		ev.setDescription("Olympiad in Geography");
		ev.setVenueId(1);
		ev.setId(5);
		
		eventService.save(ev);
	}
	
	// This class is here as a starter for testing any custom methods within the
	// EventService. Note: It is currently @Disabled!
	@Test
	public void countTest() {
		int cnt = (int)eventService.count();
		for (Event ev : eventService.findAll())
			cnt--;
		assertSame(cnt, 0);
	}
	
	@Test
	public void findOneTestFound() {
		Event ev = new Event();
		eventService.save(ev);
		assertSame(ev, eventService.findOne(ev.getId()));
	}
	
	@Test
	public void findOneTestNotFound() {
		assertSame(null, eventService.findOne(-1));
	}
	
	@Test
	public void existsByIdFalse() {
		assertFalse(eventService.existsById(0));
	}
	
	@Test
	public void existsByIdTrue() {
		assertTrue(eventService.existsById(ev.getId()));
	}
	
	@Test
	public void deleteByIdValid() {
		assertTrue(eventService.existsById(5));
		eventService.deleteById(5);
		assertFalse(eventService.existsById(5));
	}
	
	@Test
	public void deleteAllTest() {
		eventService.deleteAll();
		assertSame(0, (int)eventService.count());
	}

	@Test
	public void findAllTest() {
		eventService.deleteAll();
		
		Event eventA = new Event(); eventA.setName("Event A"); eventA.setVenueId(1); eventA.setDate(LocalDate.of(2020, 1, 1));
		Event eventB = new Event();	eventB.setName("Event B"); eventB.setVenueId(1); eventB.setDate(LocalDate.of(2020, 1, 1));
		Event eventC = new Event(); eventC.setName("Event C"); eventC.setVenueId(1); eventC.setDate(LocalDate.of(2020, 1, 1));
		
		// Insert in an arbitrary order
		eventService.save(eventB);
		eventService.save(eventC);
		eventService.save(eventA);
		
		String previous = "";
		for (Event ev : eventService.findAll()) {
			assertTrue(previous.compareTo(ev.getName()) < 0);
			previous = ev.getName();
		}	
	}

	@Test
	public void findPreviousTest() {
		eventService.deleteAll();
		
		Event ev = new Event();
		ev.setDate(LocalDate.of(1998, 1, 5));
		ev.setTime(LocalTime.now());
		ev.setName("Event Past");
		ev.setVenueId(1);
		ev.setId(100);
		eventService.save(ev);
		
		int counter = 0;
		for (Event e : eventService.findPrevious()) counter++;
		assertSame(1, counter);
		
		counter = 0;
		for (Event e : eventService.findUpcoming()) counter++;
		assertSame(0, counter);
	}
	
	@Test
	public void findUpcomingTest() {
		eventService.deleteAll();

		Event ev = new Event();
		ev.setDate(LocalDate.of(2026, 5, 6));
		ev.setTime(LocalTime.now());
		ev.setName("Event Future");
		ev.setVenueId(1);
		ev.setId(101);
		eventService.save(ev);

		int counter = 0;
		for (Event e : eventService.findPrevious()) counter++;
		assertSame(0, counter);

		counter = 0;
		for (Event e : eventService.findUpcoming()) counter++;
		assertSame(1, counter);
	}
	
	@Test
	public void findByNameTest() {
		eventService.deleteAll();
		
		Event ev1 = new Event();
		ev1.setVenueId(1); ev1.setId(0);
		ev1.setName("Event 1");
		eventService.save(ev1);
		
		Event ev2 = new Event();
		ev2.setVenueId(1); ev2.setId(1);
		ev2.setName("Event 2");
		eventService.save(ev2);
		
		int counter = 0;
		for (Event e : eventService.findByName("nt")) counter++;
		assertSame(2, counter);
		
		counter = 0;
		for (Event e : eventService.findByName("ab")) counter++;
		assertSame(0, counter);
		
		counter = 0;
		for (Event e : eventService.findByName("Event 1")) counter++;
		assertSame(1, counter);
	}
	
	@Test
	public void findByNameBeforeTest() {
		eventService.deleteAll();
		
		Event ev1 = new Event();
		ev1.setVenueId(1); ev1.setId(0);
		ev1.setDate(LocalDate.of(1998, 1, 5));
		ev1.setName("Event Past");
		eventService.save(ev1);
		
		Event ev2 = new Event();
		ev2.setVenueId(1); ev2.setId(1);
		ev2.setDate(LocalDate.of(2026, 1, 5));
		ev2.setName("Event Future");
		eventService.save(ev2);

		for (Event e : eventService.findByNameBefore("event")) 
			assertSame(e.getName(), ev1.getName());
	}

	@Test
	public void findByNameAfterTest() {
		eventService.deleteAll();
		
		Event ev1 = new Event();
		ev1.setVenueId(1); ev1.setId(0);
		ev1.setDate(LocalDate.of(1998, 1, 5));
		ev1.setName("Event Past");
		eventService.save(ev1);
		
		Event ev2 = new Event();
		ev2.setVenueId(1); ev2.setId(1);
		ev2.setDate(LocalDate.of(2026, 1, 5));
		ev2.setName("Event Future");
		eventService.save(ev2);

		for (Event e : eventService.findByNameAfter("event")) 
			assertSame(e.getName(), ev2.getName());
	}
	
	@Test
	public void findByVenueIdTest() {
		eventService.deleteAll();

		Event ev = new Event();
		ev.setVenueId(1);
		ev.setName("My Event");
		eventService.save(ev);

		for (Event e : eventService.findByVenueId(1))
			assertSame(e.getName(), ev.getName());
	}
}
