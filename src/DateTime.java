import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


//a class for getting dates and times
public class DateTime{
	public static int sec = 0;
	public static int min = 0;
	public static int hours = 0;
	public static DateFormat year = new SimpleDateFormat("yyyy");
	public static DateFormat month = new SimpleDateFormat("MM");
	public static DateFormat day = new SimpleDateFormat("dd");
	public static DateFormat hour = new SimpleDateFormat("HH");
	public static DateFormat minute = new SimpleDateFormat("mm");
	public static DateFormat second = new SimpleDateFormat("ss");
	private static Calendar cal = new GregorianCalendar();
	public static Date date = new Date();
	
	
	
	public static Integer currentSecond(){
		return cal.get(Calendar.SECOND);
		//return Integer.parseInt(second.format(date));
	}
	
	
	public static Integer currentMinute(){
		return cal.get(Calendar.MINUTE);
		//return Integer.parseInt(minute.format(date));
	}
	
	
	public static Integer currentHour(){
		return cal.get(Calendar.HOUR_OF_DAY);
		//return Integer.parseInt(hour.format(date));
	}
	
	
	public static int secondsLeft(Train train, String stopName){
		if (train != null) {
			sec = train.getPredictionByName(stopName).getTime();
			sec %= 60;
		}		
		return sec;
	}
	public static int minutesLeft(Train train, String stopName){
		if (train != null) {
			sec = train.getPredictionByName(stopName).getTime();
			min = sec/60;
		}		
		return min;
	}
	public static String arrivesAt(Train train, String stopName){
		if (train != null) {
			sec = currentSecond()+train.getPredictionByName(stopName).getTime();
			min = currentMinute()+(sec)/60;
			hours = currentHour()+((min)/60);
			min %= 60;
			sec %= 60;
		}
		return hours + ":" + min + ":" + sec;
	}
}