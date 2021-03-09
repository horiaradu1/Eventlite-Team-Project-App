package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public void save(Event e);
	
	public Event findOne(long id);
	
	public Iterable<Event> findByName(String name);
}
