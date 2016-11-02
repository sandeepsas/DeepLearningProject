package timeseriesgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class DayFilter {

	public static void main(String[] args) throws IOException {
		
		PrintWriter writer = new PrintWriter(new File ("data/proc/train_v2.csv"));

		BufferedReader bf = new BufferedReader(new FileReader("data/proc/train_v1.csv"));
		String s = new String();
		s = bf.readLine();
		int no_output_lines = 14*288;
		int cnt = 0;
		while((s=bf.readLine())!=null &&
				(s.length()!=0) ){
			if(cnt<no_output_lines){
				writer.println(s);
			}else{
				break;
			}
			cnt++;
		}
	}

}
