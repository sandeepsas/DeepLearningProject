package timeseriesgen;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class TimeSeriesPattern {

	public static Map<String, Integer> map_time_slot = new LinkedHashMap<String,Integer>();
	static PrintWriter writer;
	public static void main(String[] args) throws IOException {

		DateTime startTime_t = Constants.time_formatter_HM.parseDateTime("0000");
		DateTime endTime_t = Constants.time_formatter_HM.parseDateTime("2359");


		int cnt=0;
		while(startTime_t.isBefore(endTime_t)){
			map_time_slot.put(startTime_t.toString(Constants.time_formatter_HM).trim()+
					startTime_t.plusMinutes(5).toString(Constants.time_formatter_HM).trim(), cnt );
			startTime_t = startTime_t.plusMinutes(5);
			cnt++;
		}
		writer = new PrintWriter(new File ("data/proc/PatternOut1.txt"));

		DateTime startTime = Constants.dt_formatter.parseDateTime("2013-01-10 00:00:00");
		DateTime endTime = Constants.dt_formatter.parseDateTime("2013-01-31 00:00:01");
		/*Count number of days and initialize array*/
		int noTimeSlots = 12*24;
		int noDays = Days.daysBetween(startTime.toLocalDate(), endTime.toLocalDate()).getDays();
		System.out.println(noDays);

		ArrayList<HashMap<Integer, ArrayList<Double> >> hashMapDayTimeList = new 
				ArrayList<HashMap<Integer, ArrayList<Double> >>();

		/*Query Trips Day by day*/

		
		int dayCount = 0;
		while(startTime.isBefore(endTime)){
			if(startTime.getDayOfWeek()>5){
				startTime = startTime.plusDays(1);
				continue;
			}
			HashMap<Integer, ArrayList<Double> > mapTemp = new LinkedHashMap<>();
			DateTime endOfDay = startTime.plusDays(1);
			List<TaxiTrip>  trips = loadTrips(startTime,endOfDay);
			System.out.println("Total No of trips in the pool on "+startTime.toString(Constants.dt_formatter)+" = "+trips.size());
			startTime = startTime.plusDays(1);
			dayCount++;
			ArrayList<Double> pickUpTimeList = new ArrayList<Double>(Collections.nCopies(noTimeSlots,0.0));
			
			for(TaxiTrip i:trips){

				String f_date = (Constants.dt_formatter.parseDateTime(i.pickup_datetime)).toString(Constants.time_formatter_HM);
				/*Find the time Slot*/
				int timeSlot = getTimeSlot(f_date);
				
				//Compute Probability
				int end_mins = Constants.time_formatter_HM.parseDateTime(f_date).getMinuteOfHour();
				int loggy = end_mins%5;
				double probability = (double) loggy/5.00;
				
				if(pickUpTimeList.get(timeSlot)!=0.0){
					pickUpTimeList.add(timeSlot, probability);
				}else{
					double p = (pickUpTimeList.get(timeSlot)+probability)/2;
					pickUpTimeList.add(timeSlot, p);
				}
				
				
				
			}
			mapTemp.put(dayCount, pickUpTimeList);
			hashMapDayTimeList.add(mapTemp);
		}
		
		printHashMap(hashMapDayTimeList);
		//System.out.println(hashMapDayTimeList);


	}
	private static void printHashMap(ArrayList<HashMap<Integer, ArrayList<Double> >>  hashMapList) {
		for(HashMap<Integer, ArrayList<Double> > mappy:hashMapList ){
			for (Entry<Integer, ArrayList<Double>> entry : mappy.entrySet()) {
				//Integer key = entry.getKey();
				ArrayList<Double> value = entry.getValue();
				//writer.println(Arrays.toString(value.toArray()));
				
				for(int i = 0; i < value.size(); i++) {   
					writer.println(value.get(i));
				}  
			}
		}
		writer.close();
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
		int mm_a = mm - (mm%5);
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

		DateTime end = start.plusMinutes(5);

		int e_hh = end.getHourOfDay();
		int e_mm = end.getMinuteOfHour();
		int e_mm_a = e_mm - (e_mm%5);

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
