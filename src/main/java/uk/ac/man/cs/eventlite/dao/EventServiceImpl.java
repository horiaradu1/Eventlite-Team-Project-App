package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
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
		return eventRepository.findAllByOrderByDateAscTimeAsc();
	}
	
	@Override
	public Iterable<Event> findByName(String name){
		return eventRepository.findByNameContainingIgnoreCaseOrderByDateAscTimeAscNameAsc(name);
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
}
