package novPack;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class GenTrainDataForSingleEdge {

	public static void main(String[] args) throws IOException {


		PrintWriter label = new PrintWriter(new File("novData/sample/label_edge1.csv"));
		PrintWriter train = new PrintWriter(new File("novData/sample/train_edge1.csv"));
		PrintWriter test = new PrintWriter(new File("novData/sample/test_edge1.csv"));
		
		BufferedReader bf = new BufferedReader(new FileReader("data/grid_train.csv"));
		String s = bf.readLine();
		String[] temp = s.split(",");
		train.println(temp[0]);
		
		int total_rows = 4320;
		int count_needed = (int) (0.8*total_rows);
		int count=0;
		
		System.out.println("Total Rows = "+ total_rows);
		System.out.println("Train Count = "+ count_needed);
		
		while((s=bf.readLine())!=null && s.length()!=0){
			temp = s.split(",");
			label.println(temp[0]);
			if(count<count_needed-1){
				train.println(temp[0]);
			}else{
				break;
			}
			count++;
		}
		
		while((s=bf.readLine())!=null && s.length()!=0){
			temp = s.split(",");
			test.println(temp[0]);
		}
		
		/*close all*/
		train.close();
		bf.close();
		label.close();
		test.close();
		
		System.out.println("label = "+countLines("novData/sample/label_edge1.csv"));
		System.out.println("train = "+countLines("novData/sample/train_edge1.csv"));
		System.out.println("test = "+countLines("novData/sample/test_edge1.csv"));
	}
	
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
