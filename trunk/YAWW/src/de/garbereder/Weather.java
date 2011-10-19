package de.garbereder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Weather {

	private ForecastCondition[] conditions = new ForecastCondition[3];
	private CurrentCondition currentCondition;
	private ForecastInformation forecastInformation;
	private String city;
	private String locale;
	
	public enum Forecasts
	{
		TODAY,
		TOMORROW,
		DAY_AFTER_TOMORROW
	}
	
	public Weather( String city, String locale ) throws ClientProtocolException, IOException, ParserConfigurationException, IllegalStateException, SAXException, ExceptionWithId
	{
		for( int i = 0; i < 3; ++i )
			conditions[i] = null;
		this.city = city;
		this.locale = locale;
		updateWeather();
	}

	public void updateWeather() throws ClientProtocolException, IOException, ParserConfigurationException, IllegalStateException, SAXException, ExceptionWithId
	{
		String url = "http://www.google.com/ig/api?weather=" + URLEncoder.encode(city) + "&hl=" + locale;
		HttpClient hc = new DefaultHttpClient();
		HttpResponse rp = hc.execute(new HttpPost(url));
		if(rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
		{	
    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // @todo dynamic encoding?
            InputStreamReader isr = new InputStreamReader(rp.getEntity().getContent(), "ISO-8859-1");
            int i;
            StringBuilder sb = new StringBuilder();
            while ((i = isr.read()) != -1) {
            	sb.append((char)i);
            }
            Document dom = builder.parse(new ByteArrayInputStream(sb.toString().getBytes()));
            
            Element root = dom.getDocumentElement();
            NodeList items = root.getElementsByTagName("weather");
            if( items.getLength() == 1 ) {
                Node weatherItem = items.item(0);
                NodeList dataItems = weatherItem.getChildNodes();
                if( dataItems.getLength() == 1 )
                {
                	Node errorItem = dataItems.item(0);
                	if( "problem_cause".equals(errorItem.getNodeName()) )
                	{
                		throw new ExceptionWithId(R.string.errorCity);
                	}
                }
                else if( dataItems.getLength() == 6 )
                {
	            	ForecastInformation info = createForecastInformation(dataItems.item(0));
	            	CurrentCondition currentCondition = createCurremtCondition(dataItems.item(1),info.getUnitSystem());
	            	forecastInformation = info;
	            	this.currentCondition = currentCondition;
	            	addForecast(createForecastCondition(dataItems.item(2), info.getUnitSystem()), Weather.Forecasts.TODAY);
	            	addForecast(createForecastCondition(dataItems.item(3), info.getUnitSystem()), Weather.Forecasts.TOMORROW);
	            	addForecast(createForecastCondition(dataItems.item(4), info.getUnitSystem()), Weather.Forecasts.DAY_AFTER_TOMORROW);
                }
                else
                	throw new ExceptionWithId(R.string.noMatch);
            }
        }
	}
	
	private ForecastInformation createForecastInformation(Node item) {
		NodeList items = item.getChildNodes();
		String postalCode = null;
		String cityInformation = null;
		Date forecastDate = null;
		String unitSystem = null;
		for( int i = 0; i < items.getLength(); ++i )
		{
			Node curremt = items.item(i);
			if( "postal_code".equals(curremt.getNodeName()) )
				postalCode = curremt.getAttributes().getNamedItem("data").getNodeValue();
			else if( "city".equals(curremt.getNodeName()) )
				cityInformation = curremt.getAttributes().getNamedItem("data").getNodeValue();
			else if( "forecast_date".equals(curremt.getNodeName()) )
			{
				GregorianCalendar cal = new GregorianCalendar();
				String[] dateStr = curremt.getAttributes().getNamedItem("data").getNodeValue().split("-");
				cal.set(Integer.parseInt(dateStr[0]), Integer.parseInt(dateStr[1]), Integer.parseInt(dateStr[2]));
				forecastDate = cal.getTime();
			}
			else if( "unit_system".equals(curremt.getNodeName()) )
				unitSystem = curremt.getAttributes().getNamedItem("data").getNodeValue();
		}
		return new ForecastInformation(postalCode, cityInformation, forecastDate, unitSystem);
	}
    
    private CurrentCondition createCurremtCondition(Node item, String unitSystem) {
		NodeList items = item.getChildNodes();
		String tempStr = "temp_c";
		if( "US".equals(unitSystem) )
			tempStr = "temp_f";
		String condition = null;
		int temp = 0;
		String humidity = null;
		String iconUrl = null;
		String windCondition = null;
		for( int i = 0; i < items.getLength(); ++i )
		{
			Node curremt = items.item(i);
			if( "condition".equals(curremt.getNodeName()) )
				condition = curremt.getAttributes().getNamedItem("data").getNodeValue();
			else if( tempStr.equals(curremt.getNodeName()) )
				temp = Integer.parseInt(curremt.getAttributes().getNamedItem("data").getNodeValue());
			else if( "humidity".equals(curremt.getNodeName()) )
				humidity = curremt.getAttributes().getNamedItem("data").getNodeValue();
			else if( "icon".equals(curremt.getNodeName()) )
				iconUrl = curremt.getAttributes().getNamedItem("data").getNodeValue();
			else if( "wind_condition".equals(curremt.getNodeName()) )
				windCondition = curremt.getAttributes().getNamedItem("data").getNodeValue();
		}
		return new CurrentCondition(condition, temp, humidity, iconUrl, windCondition);
	}
    
    private ForecastCondition createForecastCondition(Node item, String unitSystem) {
		NodeList items = item.getChildNodes();
		String dayOfWeek = null;
		int low = 0;
		int high = 0;
		String iconUrl = null;
		String condition = null;
		for( int i = 0; i < items.getLength(); ++i )
		{
			Node curremt = items.item(i);
			if( "day_of_week".equals(curremt.getNodeName()) )
				dayOfWeek = curremt.getAttributes().getNamedItem("data").getNodeValue();
			else if( "condition".equals(curremt.getNodeName()) )
				condition = curremt.getAttributes().getNamedItem("data").getNodeValue();
			else if( "low".equals(curremt.getNodeName()) )
				low = Integer.parseInt(curremt.getAttributes().getNamedItem("data").getNodeValue());
			else if( "high".equals(curremt.getNodeName()) )
				high = Integer.parseInt(curremt.getAttributes().getNamedItem("data").getNodeValue());
			else if( "icon".equals(curremt.getNodeName()) )
				iconUrl = curremt.getAttributes().getNamedItem("data").getNodeValue();
		}
		return new ForecastCondition(dayOfWeek, low, high, iconUrl, condition);
	}	
	
	public String getCity() {
		return city;
	}

	public void setCity(String city) throws ClientProtocolException, IllegalStateException, IOException, ParserConfigurationException, SAXException, ExceptionWithId {
		this.city = city;
		updateWeather();
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) throws ClientProtocolException, IllegalStateException, IOException, ParserConfigurationException, SAXException, ExceptionWithId {
		this.locale = locale;
		updateWeather();
	}

	private void addForecast( ForecastCondition c, Forecasts f )
	{
		conditions[f.ordinal()] = c;
	}
	
	public ForecastCondition getForecast( Forecasts f )
	{
		return conditions[f.ordinal()];
	}
	
	public CurrentCondition getCurrentCondition() {
		return currentCondition;
	}

	public ForecastInformation getInfo() {
		return forecastInformation;
	}
	
}
