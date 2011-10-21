package de.garbereder;

import java.util.Date;

public class ForecastInformation {

	private String postalCode;
	private String cityInformation;
	private Date forecastDate;
	private String unitSystem;
	
	public ForecastInformation(String postalCode, String cityInformation,
			Date forecastDate, String unitSystem) {
		this.postalCode = postalCode;
		this.cityInformation = cityInformation;
		this.forecastDate = forecastDate;
		this.unitSystem = unitSystem;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getCityInformation() {
		return cityInformation;
	}

	public Date getForecastDate() {
		return forecastDate;
	}

	public String getUnitSystem() {
		return unitSystem;
	} 
	
}
