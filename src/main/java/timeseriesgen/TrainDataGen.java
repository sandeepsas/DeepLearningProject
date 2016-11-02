package timeseriesgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

public class TrainDataGen {
	
	public static Map<String, Integer> map_time_slot = new LinkedHashMap<String,Integer>();
	static PrintWriter writer;

	public static DateTime startTime = Constants.dt_formatter.parseDateTime("2013-01-31 00:00:00");
	public static DateTime endTime = Constants.dt_formatter.parseDateTime("2013-02-01 00:00:01");

	public static void main(String[] args) throws IOException {
		
		DateTime startTime_t = Constants.time_formatter_HM.parseDateTime("0000");
		DateTime endTime_t = Constants.time_formatter_HM.parseDateTime("2359");

		writer = new PrintWriter(new File ("data/proc/train_v1_10min_feb.csv"));
		int cnt=0;
		while(startTime_t.isBefore(endTime_t)){
			int whichDay = startTime_t.getDayOfWeek();
			if(whichDay>5){
				startTime_t = startTime_t.plusMinutes(10);
				continue;
			}
			map_time_slot.put(startTime_t.toString(Constants.time_formatter_HM).trim()+
					startTime_t.plusMinutes(10).toString(Constants.time_formatter_HM).trim(), cnt );
			startTime_t = startTime_t.plusMinutes(10);
			cnt++;
		}
		//ArrayList<Double> pickUpTimeList = new ArrayList<Double>(Collections.nCopies(noTimeSlots,0.0));
		// Fetch Data Every 5 min interval
		while(startTime.isBefore(endTime)){
			int whichDay = startTime.getDayOfWeek();
			if(whichDay>5){
				startTime = startTime.plusMinutes(10);
				continue;
			}
			DateTime intervalEnd = startTime.plusMinutes(10);
			List<TaxiTrip>  trips = loadTrips(startTime,intervalEnd);
			List<String> pickupTimeList = new ArrayList<String>();
			for(int i=0;i<trips.size();i++){
				//Populate the pickup time array and send for probability calculation
				pickupTimeList.add(trips.get(i).pickup_datetime);
			}
			Double probability = computeProbability(startTime,intervalEnd,pickupTimeList);
			String f_date = (startTime).toString(Constants.time_formatter_HM);
			/*Find the time Slot*/
			int timeSlot = getTimeSlot(f_date);

			System.out.println("Time Slot = "+timeSlot+"\t PickUpTime = "+startTime.toString(Constants.dt_formatter)+"\t probability = "+probability);
			
			startTime = startTime.plusMinutes(10);
			writer.println(probability);
		}
		writer.close();

	}
	private static Double computeProbability(DateTime startTime2,
			DateTime intervalEnd2,
			List<String> pickupTimeList) {
		List<DateTime> dateList = new ArrayList<DateTime>();
		List<DateTime> timePickUpList = new ArrayList<DateTime>();

		for(String i: pickupTimeList){
			dateList.add(Constants.dt_formatter.parseDateTime(i));
			timePickUpList.add((Constants.dt_formatter.parseDateTime(i)).plusMinutes(2));
		}

		double requestPendingDuration = 0;
		long intervalStart = startTime2.getMillis();
		long intervalEnd = intervalEnd2.getMillis();

		boolean interval_flag = false;
		for(int k=0;k< timePickUpList.size();k++){
			long timeDiff=0;
			if(timePickUpList.get(k).isBefore(intervalEnd)){

				if(k<1){
					timeDiff =  (timePickUpList.get(k).getMillis()-intervalStart);
				}else{
					timeDiff = (timePickUpList.get(k).getMillis()-intervalStart);
				}
				requestPendingDuration+=timeDiff;
				intervalStart = timePickUpList.get(k).getMillis();
			}else{
				interval_flag = true;
				requestPendingDuration = 10*60*1000;
				break;
			}
		}
		System.out.println("\n\t Pending Duration in millis = "+requestPendingDuration);
		double probability = requestPendingDuration/(60*10*1000);
		//System.out.println("\n\t Probability = "+probability);
		return probability;
	}
	public static List<TaxiTrip> loadTrips(DateTime startTime, DateTime endTime) throws IOException {
		// TODO Auto-generated method stub
		List<TaxiTrip> trips = new ArrayList<TaxiTrip>();
		BufferedReader bf = new BufferedReader(new FileReader("data/trip/TripDataID.csv"));
		String s = new String();
		s = bf.readLine();
		while((s=bf.readLine())!=null &&
				(s.length()!=0) ){
			String[] split_readline = s.split(",");
			DateTime trip_start_time =  Constants.dt_formatter.parseDateTime(split_readline[6]);

			TaxiTrip trip = new TaxiTrip();

			if(trip_start_time.compareTo(startTime)>0 &&
					trip_start_time.compareTo(endTime)<=0 	){
				trip = new TaxiTrip(split_readline[0],
						split_readline[6],
						split_readline[7],
						split_readline[8],
						split_readline[9],
						split_readline[10],
						split_readline[11],
						split_readline[12],
						split_readline[13],
						split_readline[14]);
				trips.add(trip);

			}

		}
		bf.close();
		return trips;

	}
	private static int getTimeSlot(String pickup_datetime) {
		DateTime start = Constants.time_formatter_HM.parseDateTime(pickup_datetime);

		int hh = start.getHourOfDay();
		int mm = start.getMinuteOfHour();
		int mm_a = mm - (mm%10);
		String st_key;

		if((hh<10) && (mm_a<10)){
			st_key = ""+0+hh+0+mm_a;
		}else if((hh<10)){
			st_key = ""+0+hh+mm_a;
		}else if(mm_a<10){
			st_key = ""+hh+0+mm_a;
		}else{

			st_key = ""+hh+mm_a;
		}


		//Find to which interval the start time belongs to

		DateTime end = start.plusMinutes(10);

		int e_hh = end.getHourOfDay();
		int e_mm = end.getMinuteOfHour();
		int e_mm_a = e_mm - (e_mm%10);

		String e_st_key;

		if((e_hh<10) && (e_mm_a<10)){
			e_st_key = ""+0+e_hh+0+e_mm_a;
		}else if((e_hh<10)){
			e_st_key = ""+0+e_hh+e_mm_a;
		}else if(e_mm_a<10){
			e_st_key = ""+e_hh+0+e_mm_a;
		}else{
			e_st_key = ""+e_hh+e_mm_a;
		}



		//String e_st_key = ""+e_hh+e_mm;

		String key = st_key+ e_st_key;
		/*String key = pickup_datetime.trim() +
				end.toString(Constants.time_formatter_HM).trim();*/
		//System.out.println(key);
		int res = map_time_slot.get(key);
		return res;
	}

}
