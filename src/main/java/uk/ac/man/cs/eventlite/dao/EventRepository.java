package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventRepository extends CrudRepository<Event, Long>{

	public Iterable<Event> findAllByOrderByDateAscNameAsc();
	public Iterable<Event> findByNameContainingIgnoreCaseAndDateAfterOrderByDateAscNameAsc(String name, LocalDate today);
	public Iterable<Event> findByNameContainingIgnoreCaseAndDateBeforeOrderByDateDescNameAsc(String name, LocalDate today);
	public Iterable<Event> findByNameContainingIgnoreCaseOrderByDateAscNameAsc(String name);
	public Iterable<Event> findByDateBeforeOrderByDateDescNameAsc(LocalDate today);
	public List<Event> findByDateAfterOrderByDateAscNameAsc(LocalDate today, Pageable number);
	public Iterable<Event> findByDateAfterOrderByDateAscNameAsc(LocalDate today);
	public Iterable<Event> findByVenueId(long venueId);
	
}
