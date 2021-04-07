package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {
	
	@Autowired
	private VenueRepository venueRepository;
	
	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	private final static String DATA = "data/venues.json";

	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAll();
	}
	
	@Override
	public void save(Venue venue) {
		venueRepository.save(venue);
	}
	
	@Override
	public Venue findOne(long id) {
		return venueRepository.findById(id).orElse(null);
	}
	
	@Override
	public Iterable<Venue> findTopVenues(int n) {
		Pageable numberOfVenues = PageRequest.of(0, n);
		return venueRepository.findTopVenues(numberOfVenues);
	}
	
	@Override
	public Iterable<Integer> findNumberEvents(int n) {
		return venueRepository.countEvents(n);
	}
	
	
}
