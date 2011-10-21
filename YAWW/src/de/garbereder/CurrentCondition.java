package de.garbereder;

public class CurrentCondition {
	private String condition;
	private int temp;
	private String humidity;
	private String iconUrl;
	private String windCondition;

	/*
	 * <current_conditions>
	 * <condition data="Regen"/>
	 * <temp_f data="52"/>
	 * <temp_c data="11"/>
	 * <humidity data="Luftfeuchtigkeit: 100 %"/>
	 * <icon data="/ig/images/weather/rain.gif"/>
	 * <wind_condition data="Wind: SW mit 10 km/h"/>
	 * </current_conditions>
	 */
	
	public CurrentCondition( String condition, int temp,
							String humidity, String iconUrl, String windCondition )
	{
		this.condition = condition;
		this.temp = temp;
		this.humidity = humidity;
		this.iconUrl = iconUrl;
		this.windCondition = windCondition;
	}

	public String getCondition() {
		return condition;
	}

	public int getTemp() {
		return temp;
	}

	public String getHumidity() {
		return humidity;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public String getWindCondition() {
		return windCondition;
	}
	
}
