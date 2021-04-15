package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();
	
	public void save(Venue venue);

	public Venue findOne(long id);
	
	public Iterable<Venue> findTopVenues(int n);
	
	public Iterable<Integer> findNumberEvents(int n);
	
	public Iterable<Venue> findByName(String name);

	public void deleteById(long id);
}