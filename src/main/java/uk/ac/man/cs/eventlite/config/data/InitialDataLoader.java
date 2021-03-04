package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;

import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;

@Component
@Profile({ "default", "test" })
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (eventService.count() > 0) {
			log.info("Database already populated. Skipping data initialization.");
			return;
		}

		// Build and save initial models here.
		Venue venue = new Venue();
		venue.setName("Kilburn, G23");
		venue.setCapacity(80);
		venue.setId(1);
		venueService.save(venue);
		
		Venue online = new Venue();
		online.setName("Online");
		online.setCapacity(100000);
		online.setId(2);
		venueService.save(online);
		
		Event ev = new Event();
		ev.setDate(LocalDate.now());
		ev.setTime(LocalTime.now());
		ev.setVenueId(1);

		ev.setId(47);
		ev.setName("ONG 2018");
		ev.setDescription("OGN Something");
		eventService.save(ev);

		ev.setId(48);
		ev.setName("Code Jam");
		ev.setDescription("Fun codejam");
		eventService.save(ev);

		ev.setId(49);
		ev.setName("ONI 2019");
		ev.setDescription("ONI Something IDK");
		eventService.save(ev);
	}
}
