package uk.ac.man.cs.eventlite.entities;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "venues")
public class Venue {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String name;
	
	private String street;
	
	private String postcode;

	private int capacity;

	private double latitude;
	
	private double longitude;
	
	public Venue() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}
	
	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public static String validation(Venue v) {
		if (v.name == null || v.name.length() == 0) {
			return "Must enter a name";
		}
		if (v.name.length() >= 256) {
			return "Venue name is too long";
		}
		if (v.street == null) {
			return "Must enter a streetname";
		}
		if (v.street.length() >= 300) {
			return "Streetname is too long";
		}
		if (v.postcode == null) {
			return "Must enter a postcode";
		}
		if (v.capacity <= 0) {
			return "Capacity must be an integer greater than 0";
		}
		return "";
	}
}
