package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

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
	public void existsByIdFalse() {
		assertFalse(eventService.existsById(0));
	}
	
	@Test
	public void existsByIdTrue() {
		Event ev = new Event();
		ev.setDate(LocalDate.now());
		ev.setTime(LocalTime.now());
		ev.setVenueId(1);

		ev.setId(0);
		ev.setName("ONG 2018");
		eventService.save(ev);
		assertTrue(eventService.existsById(0));
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
}
