package onDemandMob;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ComputeRMSE {

	public static void main(String[] args) throws IOException {
		
		BufferedReader bf_train = new BufferedReader(new FileReader("data/sample/train1.csv"));
		BufferedReader bf_label = new BufferedReader(new FileReader("data/sample/label1.csv"));
		String s_train = new String();
		String s_label = new String();
		bf_train.readLine();
		while((s_train=bf_train.readLine())!=null &&
				(s_train.length()!=0) && (s_label=bf_label.readLine())!=null && (s_label.length()!=0) ){
			try {
				String[] s_split_train = s_train.split(",");
				String[] s_split_label = s_label.split(",");
				for(int k=0;k<s_split_train.length;k++){
					double train_prob =Double.parseDouble(s_split_train[k]);
					double label_prob =Double.parseDouble(s_split_label[k]);
					
					double diff = train_prob - label_prob;
					
					if(diff!=0){
						System.out.println("WTH");
					}
					
					//System.out.println();
				}
			} catch (NumberFormatException e) {
				System.out.println("NFE");
				e.printStackTrace();
			}
		}
		bf_train.close();
		bf_label.close();
	}

}
