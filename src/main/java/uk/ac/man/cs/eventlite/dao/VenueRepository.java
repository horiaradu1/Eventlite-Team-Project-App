package uk.ac.man.cs.eventlite.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueRepository extends CrudRepository<Venue, Long> {
	
	@Query("SELECT v FROM Venue v, Event e WHERE v.id = e.venue GROUP BY v.id ORDER BY COUNT(e.id) DESC")
	public List<Venue> findTopVenues(Pageable number);
	
	
	@Query(value = "SELECT COUNT(events.venueid) FROM events, venues WHERE events.venueid = venues.id GROUP BY venues.id ORDER BY COUNT(events.id) DESC LIMIT :number", nativeQuery = true)
	public List<Integer> countEvents(@Param("number") int number);
	 
	
}
