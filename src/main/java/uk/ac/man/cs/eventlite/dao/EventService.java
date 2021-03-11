package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public Iterable<Event> findUpcoming();
	
	public Iterable<Event> findPrevious();
	
	public void save(Event e);
	
	public Event findOne(long id);
	
	public boolean existsById(long id);
	
	public void deleteById(long id);

	public Iterable<Event> findByName(String name);
	
	public Iterable<Event> findByNameAfter(String name);
	
	public Iterable<Event> findByNameBefore(String name);
}
