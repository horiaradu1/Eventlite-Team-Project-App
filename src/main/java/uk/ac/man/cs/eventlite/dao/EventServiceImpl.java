package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	private final static String DATA = "data/events.json";
	
	@Autowired
	private EventRepository eventRepository;

	@Override
	public long count() {
		return eventRepository.count();
	}

	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findAllByOrderByDateAscNameAsc();
	}
	
	@Override
	public Iterable<Event> findUpcoming() {
		return eventRepository.findByDateAfterOrderByDateAscNameAsc(LocalDate.now());
	}

	@Override
	public Iterable<Event> findPrevious() {
		return eventRepository.findByDateBeforeOrderByDateDescNameAsc(LocalDate.now());
	}
	
	@Override
	public Iterable<Event> findByName(String name) {
		return eventRepository.findByNameContainingIgnoreCaseOrderByDateAscNameAsc(name);
	}

	@Override
	public Iterable<Event> findByNameAfter(String name){
		return eventRepository.findByNameContainingIgnoreCaseAndDateAfterOrderByDateAscNameAsc(name, LocalDate.now());
	}

	@Override
	public Iterable<Event> findByNameBefore(String name){
		return eventRepository.findByNameContainingIgnoreCaseAndDateBeforeOrderByDateDescNameAsc(name, LocalDate.now());
	}
	
	@Override 
	public void save(Event e){
		eventRepository.save(e);
	}
	
	@Override
	public Event findOne(long id) {
		return eventRepository.findById(id).orElse(null);
	}
	
	@Override
	public boolean existsById(long id) { 
		return eventRepository.existsById(id);	
	}
	
	@Override
	public void deleteById(long id) { 
		eventRepository.deleteById(id);	
	}
	
	@Override
	public void deleteAll() { 
		eventRepository.deleteAll();	
	}

	@Override
	public Iterable<Event> findByVenueId(long venueId) {
		return eventRepository.findByVenueId(venueId);
	}
}
