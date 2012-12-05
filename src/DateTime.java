import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
//a class for getting dates and times
public class DateTime{
	public static int sec = 0;
	public static int min = 0;
	public static DateFormat year = new SimpleDateFormat("yyyy");
	public static DateFormat month = new SimpleDateFormat("MM");
	public static DateFormat day = new SimpleDateFormat("dd");
	public static DateFormat hour = new SimpleDateFormat("HH");
	public static DateFormat minute = new SimpleDateFormat("mm");
	public static DateFormat second = new SimpleDateFormat("ss");
	public static Date date = new Date();
	public static Integer currentSecond(){
		return Integer.parseInt(second.format(date));
	}
	public static Integer currentMinute(){
		return Integer.parseInt(minute.format(date));
	}
	public static Integer currentHour(){
		return Integer.parseInt(hour.format(date));
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
			sec = train.getPredictionByName(stopName).getTime();
			min = sec/60;
			sec %= 60;
		}
		return currentHour() + ":" + (currentMinute()+minutesLeft(train,stopName)) + ":" + (currentSecond()+secondsLeft(train,stopName));
	}
}