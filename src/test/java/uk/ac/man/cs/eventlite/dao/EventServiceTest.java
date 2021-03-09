package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertSame;

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
		ev.setVenueId(1);

		ev.setId(0);
		ev.setName("ONG 2018");
		eventService.save(ev);
		assertSame(ev, eventService.findOne(0));
	}
	
	@Test
	public void findOneTestNotFound() {
		assertSame(null, eventService.findOne(-1));
	}
	
	@Test
	public void findByNameTest() {
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
	
	
}
