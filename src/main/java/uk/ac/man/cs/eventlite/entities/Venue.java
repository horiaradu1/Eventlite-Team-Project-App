package uk.ac.man.cs.eventlite.entities;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
		this.addressGeocode();
	}
	
	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
		this.addressGeocode();
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
		if (v.street == null || v.street.length() == 0) {
			return "Must enter a streetname";
		}
		if (v.street.length() >= 300) {
			return "Streetname is too long";
		}
		if (v.postcode == null || v.postcode.length() == 0) {
			return "Must enter a postcode";
		}
		if (v.postcode.length() >= 256) {
			return "Venue name is too long";
		}
		if (v.capacity < 0) {
			return "Capacity must be an integer greater than 0";
		}
		return "";
	}
	
	// Call this method to set latitude and longitude from the address
	public void addressGeocode() {
		// Assure that postcode and street are set
		if (Venue.validation(this).length() == 0) {
			MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken("pk.eyJ1IjoiaG9yaWFyYWR1IiwiYSI6ImNrbmV2NmI4MDF2NW0yd211aXdqM3lyOWcifQ.eH2LOcxZRqCa0LvHngEZHg")
				.query(postcode + " " + street) 
				.build();
			
			mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
				@Override
				public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
					List<CarmenFeature> results = response.body().features();
					if (results.size() > 0) {
						// Set the latitude and longitude
						Point firstResultPoint = results.get(0).center();
						setCoordinates(firstResultPoint);
					} 
				}
			 
				@Override
				public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
					throwable.printStackTrace();
				}
			});
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void setCoordinates(Point p) {
		latitude = p.latitude();
		longitude = p.longitude();
	}
}
