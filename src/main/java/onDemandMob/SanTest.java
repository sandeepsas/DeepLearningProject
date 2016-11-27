package onDemandMob;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.canova.api.records.reader.SequenceRecordReader;
import org.canova.api.records.reader.impl.CSVSequenceRecordReader;
import org.canova.api.split.FileSplit;
import org.deeplearning4j.datasets.canova.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import timeseriesgen.TaxiTrip;


public class SanTest {
    public static void main( String[] args ) throws Exception {
        int lstmLayerSize = 20;					//Number of units in each GravesLSTM layer
        int miniBatchSize = 32;						//Size of mini batch to use when  training
        int tbpttLength = 30;                       //Length for truncated backpropagation through time. i.e., do parameter updates ever 50 characters
        int numEpochs = 100;							//Total number of training epochs
        new Random(12345);

        //Get the dataset using the record reader. CSVSequenceRecordReader handles loading/parsing
        int numLinesToSkip = 0;
        String delimiter = ",";
        SequenceRecordReader featureReader = new CSVSequenceRecordReader(numLinesToSkip, delimiter);
        SequenceRecordReader labelReader = new CSVSequenceRecordReader(numLinesToSkip, delimiter);
        featureReader.initialize(new FileSplit(new File("data/proc/train_v1_10min.csv")));
        labelReader.initialize(new FileSplit(new File("data/proc/label_v1_10min.csv")));

        int numPossibleLabels = 0;
        boolean regression = true;
        DataSetIterator iter = new SequenceRecordReaderDataSetIterator(featureReader, labelReader, miniBatchSize, numPossibleLabels, regression);

        //Set up network configuration:
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(10)
            .learningRate(0.01)
            .rmsDecay(0.95)
            .seed(12345)
            .regularization(true)
            .l2(0.001)
            .weightInit(WeightInit.XAVIER)
            .updater(Updater.RMSPROP)
            .list(3)
            .layer(0, new GravesLSTM.Builder().nIn(iter.inputColumns()).nOut(lstmLayerSize)
                .activation("tanh").build())
            .layer(1, new GravesLSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                .activation("tanh").build())
            .layer(2, new RnnOutputLayer.Builder(LossFunction.MSE).activation("identity")        //MCXENT + softmax for classification
            .nIn(lstmLayerSize).nOut(iter.inputColumns()).build())
            .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength).tBPTTBackwardLength(tbpttLength)
            .pretrain(false).backprop(true)
            .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(10));

        //Print the  number of parameters in the network (and for each layer)
        Layer[] layers = net.getLayers();
        int totalNumParams = 0;
        for( int i=0; i<layers.length; i++ ){
            int nParams = layers[i].numParams();
            System.out.println("Number of parameters in layer " + i + ": " + nParams);
            totalNumParams += nParams;
        }
        System.out.println("Total number of network parameters: " + totalNumParams);
        
        
        /*Read Future Sample input data*/
        List<Double> future_prob = new ArrayList<Double>();
		BufferedReader bf = new BufferedReader(new FileReader("data/proc/train_v1_10min_feb.csv"));
		String s = new String();
		while((s=bf.readLine())!=null &&
				(s.length()!=0) ){
			future_prob.add(Double.parseDouble(s));
		}
        
        //Sandeep Training
        int miniBatchNumber = 0;
        for( int i=0; i<numEpochs; i++ ){
        	System.out.println("\n\n\n EPOCH ============================================================= "+ i+"\n\n\n");
            while(iter.hasNext()){
                DataSet ds = iter.next();
                net.fit(ds);
                /*{1,2} dimension of matrix*/
/*                INDArray nextInput = Nd4j.create(new double[]{0.4}, new int[]{1, 1});
                INDArray output = net.rnnTimeStep(nextInput);
                System.out.println(output);*/

            }
            iter.reset();	//Reset iterator for another epoch
        }
        
        
        INDArray inputMatrix = iter.next().getFeatureMatrix().slice(0);
//      System.out.println(" // " + inputMatrix.getColumn(0) + " // " + inputMatrix.getRow(0));
      for (int i = 0; i < inputMatrix.columns(); i++){
          net.rnnTimeStep(inputMatrix.getColumn(i).transpose());
      }
      PrintWriter writer = new PrintWriter(new File ("data/proc/rnn_v1_10min_epoch100.txt"));
        INDArray output2 =  net.rnnTimeStep(Nd4j.create(new double[]{future_prob.get(0)}, new int[]{1, 1},'f'));
        int no_out = future_prob.size();
        for(int k =1;k<no_out;k++){
        	writer.println(output2);
        	output2 =  net.rnnTimeStep(Nd4j.create(new double[]{future_prob.get(k)}, new int[]{1, 1},'f'));
        }
        writer.close();
        System.out.println("\n\nExample complete");
    }

}
