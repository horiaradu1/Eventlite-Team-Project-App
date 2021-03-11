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

	private void addVenue(int venueId, String name, int capacity) {
		Venue ven = new Venue();
		ven.setName(name);
		ven.setCapacity(capacity);
		ven.setId(venueId);
		venueService.save(ven);	
	}

	private void addEvent(int eventId, int venueId, LocalDate mydate, LocalTime mytime, String name, String descript) {
		Event ev = new Event();
		ev.setId(eventId);
		ev.setVenueId(venueId);
		ev.setDate(mydate);
		ev.setTime(mytime);
		ev.setName(name);
		ev.setDescription(descript);
		eventService.save(ev);
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (eventService.count() > 0) {
			log.info("Database already populated. Skipping data initialization.");
			return;
		}

		// Build and save initial models here.

		LocalDate date1 = LocalDate.of(2017, 1, 13);  
		LocalDate date2 = LocalDate.of(2018, 5, 10);
		LocalDate date3 = LocalDate.of(2019, 4, 26);
		LocalDate date4 = LocalDate.of(2022, 2, 14);
		LocalTime time1 = LocalTime.of(9, 0);
		LocalTime time2 = LocalTime.of(11, 0);
		LocalTime time3 = LocalTime.of(16, 30);

		addVenue(1, "Kilburn, G23", 80);
		addVenue(2, "Online", 100000);

		addEvent(47, 1, date3, time3, "ONI 2019", "Olympiad in Informatics (Suceava)");
		addEvent(48, 1, date1, time2, "ONI 2017", "Olympiad in Informatics (Brasov)");
		addEvent(49, 2, date4, time3, "Code Jam 2022", "Contest organized by Google");
		addEvent(50, 1, date2, time2, "ONM 2018", "Mathematical Olympiad (Timisoara)");
		addEvent(51, 1, date2, time1, "ONG 2018", "National Geography Olympiad");
		addEvent(52, 2, date4, time2, "World Cup 2022", "Football Competition (Dubai)");
		addEvent(53, 1, date4, time1, "ONM 2022", "Mathematical Olympiad (Medgidia)");
	}
}
