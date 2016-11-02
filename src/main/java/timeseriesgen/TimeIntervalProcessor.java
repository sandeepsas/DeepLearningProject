package timeseriesgen;

import java.util.*;

import org.joda.time.*;

public class TimeIntervalProcessor {

	public static void main(String[] args) {

		List<String> dateStrList = new ArrayList<String>();

		dateStrList.add("2013-01-10 08:50:02");
		dateStrList.add("2013-01-10 08:50:40");
		dateStrList.add("2013-01-10 08:51:00");
		dateStrList.add("2013-01-10 08:51:59");

		//Assume the requests are satisfied in 2mins

		List<DateTime> dateList = new ArrayList<DateTime>();
		List<DateTime> timePickUpList = new ArrayList<DateTime>();

		for(String i: dateStrList){
			dateList.add(Constants.dt_formatter.parseDateTime(i));
			timePickUpList.add((Constants.dt_formatter.parseDateTime(i)).plusMinutes(2));
		}

		for (int j=0;j<dateList.size();j++){
			System.out.println("Start Time ="+dateList.get(j).toString(Constants.dt_formatter)
					+"\tEnd Time = "+timePickUpList.get(j).toString(Constants.dt_formatter));
		}
		//While populating the trips, take those picked up at pick up+ 2 min
		DateTime requestStartForInterval = Constants.dt_formatter.parseDateTime("2013-01-10 08:50:12");//Pickup by request from previous interval

		double requestPendingDuration = 0;
		long intervalStart = (Constants.dt_formatter.parseDateTime("2013-01-10 08:50:00")).getMillis();
		long intervalEnd = (Constants.dt_formatter.parseDateTime("2013-01-10 08:55:00")).getMillis();
		
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
				requestPendingDuration = 5*60*1000;
				break;
			}
		}
		System.out.println("\n\t Pending Duration in millis = "+requestPendingDuration);
		double probability = requestPendingDuration/(60*5*1000);
		System.out.println("\n\t Probability = "+probability);
	}

}
