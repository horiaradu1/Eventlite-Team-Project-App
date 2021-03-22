package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.assertj.core.internal.Iterables;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private EventService eventService;

	// This class is here as a starter for testing any custom methods within the
	// EventService. Note: It is currently @Disabled!
	@Test
	public void findOneTestFound() {
		Event ev = new Event();
		ev.setDate(LocalDate.now());
		ev.setTime(LocalTime.now());
		//ev.setVenueId(1);

		//ev.setId(0);
		ev.setName("ONG 2018");
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
		Event ev = new Event();
		ev.setDate(LocalDate.now());
		ev.setTime(LocalTime.now());
		ev.setVenueId(1);

		//ev.setId(0);
		ev.setName("ONG 2018");
		eventService.save(ev);
		assertTrue(eventService.existsById(ev.getId()));
	}
	
	@Test
	public void deleteByIdValid() {
		Event ev = new Event();
		ev.setDate(LocalDate.now());
		ev.setTime(LocalTime.now());
		ev.setVenueId(1);

		ev.setId(5);
		ev.setName("ONG 2018");
		eventService.save(ev);
		assertTrue(eventService.existsById(5));
		
		eventService.deleteById(5);
		assertFalse(eventService.existsById(5));
	}

	@Test
	public void findByNameTest() {
		eventService.deleteAll();
		
		Event ev = new Event();
		ev.setDate(LocalDate.now());
		ev.setTime(LocalTime.now());
		ev.setVenueId(1);
		ev.setId(0);
		ev.setName("Event 1");
		eventService.save(ev);
		
		Event ev2 = new Event();
		ev2.setDate(LocalDate.now());
		ev2.setTime(LocalTime.now());
		ev2.setVenueId(1);
		ev2.setId(1);
		ev2.setName("Event 2");
		eventService.save(ev2);
		
		int counter = 0;
		for (Event e : eventService.findByName("nt"))
			counter++;
		assertSame(2, counter);
		
		counter = 0;
		for (Event e : eventService.findByName("ab"))
			counter++;
		assertSame(0, counter);
		
		counter = 0;
		for (Event e : eventService.findByName("Event 1"))
			counter++;
		assertSame(1, counter);
	}

	@Test
	public void checkPreviousEvents() {
		eventService.deleteAll();

		Event ev = new Event();
		ev.setDate(LocalDate.of(1998, 1, 5));
		ev.setTime(LocalTime.now());
		ev.setName("Event Test");
		ev.setVenueId(1);
		ev.setId(100);
		eventService.save(ev);
		
		int counter;

		counter = 0;
		for (Event e : eventService.findPrevious()) counter++;
		assertSame(1, counter);

		counter = 0;
		for (Event e : eventService.findUpcoming()) counter++;
		assertSame(0, counter);
	}
	
	@Test
	public void checkUpcomingEvents() {
		eventService.deleteAll();

		Event ev = new Event();
		ev.setDate(LocalDate.of(2026, 5, 6));
		ev.setTime(LocalTime.now());
		ev.setName("Event Test");
		ev.setVenueId(1);
		ev.setId(101);
		eventService.save(ev);
		
		int counter;

		counter = 0;
		for (Event e : eventService.findPrevious()) counter++;
		assertSame(0, counter);

		counter = 0;
		for (Event e : eventService.findUpcoming()) counter++;
		assertSame(1, counter);
	}
}
