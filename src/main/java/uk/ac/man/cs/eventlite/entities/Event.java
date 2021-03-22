package uk.ac.man.cs.eventlite.entities;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
@Entity
@Table(name = "events")
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime time;

	private String name;
	
	private long venueId;
	
	private String description;
	
	@ManyToOne
	@JoinColumn(name="venueId", insertable=false, updatable=false)
	public Venue venue;

	public Event() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}
	
	public String getDateString() { 
		String month = date.getMonth().toString();
		month = month.substring(0, 1) + month.substring(1).toLowerCase(); // Capitalise only first letter
		String day = date.getDayOfMonth() + getDayNumberSuffix(date.getDayOfMonth()); // Add superscript
		return day + " " + month + " " + date.getYear();
	}

	private String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "<sup>th</sup>";
        }
        switch (day % 10) {
            case 1:
                return "<sup>st</sup>";
            case 2:
                return "<sup>nd</sup>";
            case 3:
                return "<sup>rd</sup>";
            default:
                return "<sup>th</sup>";
        }
    }

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Venue getVenue() {
		return this.venue;
	}

	public void setVenue(Venue i) {
		this.venue = i;
	}
	
	public void setVenueId(long i) {
		this.venueId = i;
	}
	
	public long getVenueId() {
		return this.venueId;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String desc) {
		this.description = desc;
	}
	
	public static String validation(Event e) {
		if (e.name == null || e.name.length() == 0) {
			return "Event name is not valid";
		}
		if (e.name.length() >= 256) {
			return "Event name is too long";
		}
		if (e.date == null) {
			return "Event date is not valid";
		}
		if (!e.date.isAfter(LocalDate.now())) {
			return "Event date must be in the future";
		}
		if (e.venue == null) {
			return "Event venue is not valid";
		}
		if (e.description.length() >= 500) {
			return "Event description is too long";
		}
		return "";
	}
}
