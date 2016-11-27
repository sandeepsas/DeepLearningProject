package timeseriesgen;
import java.io.BufferedInputStream;
/**
 * Constants
 * 
 * @author Sandeep Sasidharan
 *
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class Constants {
	
	/*Data Directories*/
	public static final File data_folder = new File("E:/Data Repo/Taxi-Trajectories/NYC/FOIL2013/FOIL2013/Trip-Files");
	public static final File[] csv_files = data_folder.listFiles();
	
	/*LatLong for LaG airport*/
	public static double LaG_lat = 40.776927;
	public static double LaG_lng = -73.873966;
	
	/*Time Zone Constants*/
	public static DateTimeFormatter dt_formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	public static DateTimeFormatter date_formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
	public static DateTimeFormatter time_formatter = DateTimeFormat.forPattern("HH:mm:ss");
	public static DateTimeFormatter time_formatter_HM = DateTimeFormat.forPattern("HHmm");
	
	public static int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}

}
