package timeseriesgen;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.joda.time.DateTime;

public class GenTimeSeries {

	public static HashMap<String, Integer> map_time_slot = new HashMap<String,Integer>();

	public static void main(String[] args) throws IOException {
		PrintWriter writer = new PrintWriter(new File ("data/proc/out1.txt"));
		PrintWriter writer_noweek = new PrintWriter(new File ("data/proc/out_noweek1.txt"));

		DateTime startTime_t = Constants.time_formatter_HM.parseDateTime("0000");
		DateTime endTime_t = Constants.time_formatter_HM.parseDateTime("2359");


		int cnt=0;
		while(startTime_t.isBefore(endTime_t)){
			map_time_slot.put(startTime_t.toString(Constants.time_formatter_HM).trim()+
					startTime_t.plusMinutes(5).toString(Constants.time_formatter_HM).trim(), cnt );
			startTime_t = startTime_t.plusMinutes(5);
/*			System.out.println(startTime_t.toString(Constants.time_formatter_HM).trim()+
					startTime_t.plusMinutes(10).toString(Constants.time_formatter_HM).trim()+"      "+ cnt);*/
			cnt++;
		}

		DateTime startTime = Constants.dt_formatter.parseDateTime("2013-01-10 00:00:00");
		DateTime endTime = Constants.dt_formatter.parseDateTime("2013-02-09 23:59:59");
		List<TaxiTrip>  trips = loadTrips(startTime,endTime);
		System.out.println("Total No of trips in the pool = "+trips.size());

		HashMap<Pair<Integer, Integer>, Integer> map = new HashMap<>();
		HashMap<Integer, Pair<Integer, Double>> map_noweek = new HashMap<>();

		for(TaxiTrip i:trips){

			String f_date = (Constants.dt_formatter.parseDateTime(i.pickup_datetime)).toString(Constants.time_formatter_HM);
			int dayOfWeek = (Constants.dt_formatter.parseDateTime(i.pickup_datetime)).getDayOfWeek();
			//System.out.println("Trip = "+i+"  Time = "+f_date);
			int timeSlot = getTimeSlot(f_date);
			//Compute Probability
			int end_mins = Constants.time_formatter_HM.parseDateTime(f_date).getMinuteOfHour();
			int loggy = end_mins%5;
			double probability = (double) loggy/5.00;
			
			System.out.println("Time = "+i.pickup_datetime+"   Slot = "+timeSlot+"  Probability = "+probability);
			Pair<Integer, Integer> key_day_slot = new Pair<Integer, Integer>(dayOfWeek,timeSlot);
			
			if(map.containsKey(key_day_slot)){
				map.put(key_day_slot, map.get(key_day_slot) + 1);
			}else{
				map.put(key_day_slot, 1);
			}
			if(map_noweek.containsKey(timeSlot)){
				int n = map_noweek.get(timeSlot).getL()+1;
				double p = map_noweek.get(timeSlot).getR()+probability;
				map_noweek.put(timeSlot, new Pair<Integer, Double>(n,p));
			}else{
				map_noweek.put(timeSlot, new Pair<Integer, Double>(1,probability));
			}

		}
		//Print output
		for (Entry<Pair<Integer, Integer>, Integer> entry : map.entrySet()) {
			Pair<Integer, Integer> key = entry.getKey();
			Integer value = entry.getValue();
			writer.println(key.getL()+","+key.getR()+","+value);
		}
	    writer.close();
	    for (Entry<Integer, Pair<Integer, Double>> entry : map_noweek.entrySet()) {
			int key = entry.getKey();
			int value = entry.getValue().getL();
			double pp = entry.getValue().getR();
			double values = pp/value;
			writer_noweek.println(key+","+values);
		}
	    writer_noweek.close();
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

}

