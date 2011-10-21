package de.garbereder;

public class ForecastCondition {

	/*
	 * <forecast_conditions>
	 * <day_of_week data="So."/>
	 * <low data="14"/>
	 * <high data="23"/>
	 * <icon data="/ig/images/weather/sunny.gif"/>
	 * <condition data="Klar"/>
	 * </forecast_conditions>
	 */
	
	private String dayOfWeek;
	private int low;
	private int high;
	private String iconUrl;
	private String condition;
	
	public ForecastCondition(String dayOfWeek, int low, int high,
			String iconUrl, String condition) {
		this.dayOfWeek = dayOfWeek;
		this.low = low;
		this.high = high;
		this.iconUrl = iconUrl;
		this.condition = condition;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public int getLow() {
		return low;
	}

	public int getHigh() {
		return high;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public String getCondition() {
		return condition;
	}
	
}
