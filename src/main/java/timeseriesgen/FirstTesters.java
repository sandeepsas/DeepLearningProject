package timeseriesgen;

import java.io.*;

public class FirstTesters {
	
	static int commaCnt = 0;

	public static void main(String[] args) throws IOException {
		
		
		PrintWriter label = new PrintWriter(new File("data/sample/label1.csv"));;
		PrintWriter train = new PrintWriter(new File("data/sample/train1.csv"));;
		PrintWriter test = new PrintWriter(new File("data/sample/test1.csv"));;
		
		//Count number of lines in a file
		BufferedReader bf = new BufferedReader(new FileReader("data/grid_train.csv"));
		String s = removeComma(bf.readLine());
		
		//Split and P

		//int train_percent = (int) (4320*0.8);
		int train_percent = (int) (100*0.8);
		int cnt = 0;
		
		train.println(s);
		if(s.split(",").length!=61650)
			System.out.println("# of columns ="+s.split(",").length);
		while((s=removeComma(bf.readLine()))!=null && s.length()!=0){
			if(s.split(",").length!=61650)
				System.out.println("# of columns ="+s.split(",").length);
			train.println(s);
			label.println(s);
			if(cnt>train_percent){
				break;
			}
			cnt++;
			//System.out.println(cnt);
		}
		String first_test_last_label = removeComma(bf.readLine());
		if(first_test_last_label.split(",").length!=61650)
			System.out.println("# of columns ="+first_test_last_label.split(",").length);
		label.println(first_test_last_label);
		test.println(first_test_last_label);
		
		int test_cnt = 0;
		while((s=removeComma(bf.readLine()))!=null && s.length()!=0){
			
			if(s.split(",").length!=61650)
				System.out.println("# of columns ="+s.split(",").length);
			if(test_cnt>train_percent){
				break;
			}
			test_cnt++;
			test.println(s);
			//System.out.println(test_cnt);
		}
		
		
		
		label.close();
		train.close();
		test.close();
		
		//System.out.println("main = "+countLines("data/grid_train.csv"));
		System.out.println("label = "+countLines("data/sample/label1.csv"));
		System.out.println("train = "+countLines("data/sample/train1.csv"));
		System.out.println("test = "+countLines("data/sample/test1.csv"));
		System.out.println(commaCnt);
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
	
	public static String removeComma(String str) {
	    if (str != null && str.length() > 0 && str.charAt(str.length()-1)==',') {
	      str = str.substring(0, str.length()-1);
	      commaCnt++;
	    }
	    return str;
	}

}
