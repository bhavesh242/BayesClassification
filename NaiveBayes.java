import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NaiveBayes {

	public static void main(String[] args) throws Exception {

		if (args.length != 4) {
			System.out.println("Please Provide 4 arguments in the following order : ");
			System.out.println("TRAIN TEST MODEL-FILE RESULT-FILE");
			System.exit(0);
		}
		String train = "./" + args[0];
		FileReader ftrain = new FileReader(train);
		BufferedReader brtrain = new BufferedReader(ftrain);
		ArrayList<ArrayList<String>> trainData = new ArrayList<ArrayList<String>>();
		String line = null;
		while ((line = brtrain.readLine()) != null) {
			String[] lineData = line.split(",");
			ArrayList<String> lineDataList = new ArrayList<String>(Arrays.asList(lineData));
			trainData.add(lineDataList);
		}
		String test = "./" + args[1];
		FileReader ftest = new FileReader(test);
		BufferedReader brtest = new BufferedReader(ftest);
		ArrayList<ArrayList<String>> testData = new ArrayList<ArrayList<String>>();
		while ((line = brtest.readLine()) != null) {
			String[] lineData = line.split(",");
			ArrayList<String> lineDataList = new ArrayList<String>(Arrays.asList(lineData));
			testData.add(lineDataList);
		}

		ClassiferBayes cb = new ClassiferBayes();
		cb.classify(trainData, testData, "./" +args[2], "./" + args[3]);
		brtest.close();
		ftest.close();
		brtrain.close();
		ftrain.close();
	}
}

class ClassiferBayes {
	public void classify(ArrayList<ArrayList<String>> trainData, ArrayList<ArrayList<String>> testData, String mfile,
			String rfile) throws Exception {
		ArrayList<HashMap<String, int[]>> features = new ArrayList<HashMap<String, int[]>>();

		for (int i = 0; i < trainData.get(0).size(); i++) {
			HashMap<String, int[]> hmap = new HashMap<String, int[]>();
			for (int j = 1; j < trainData.size(); j++) {
				if (trainData.get(j).get(trainData.get(0).size()-1).equalsIgnoreCase("0")) {
					if (hmap.get(trainData.get(j).get(i)) == null) {
						hmap.put(trainData.get(j).get(i), new int[] { 1, 0 });
					} else {
						hmap.get(trainData.get(j).get(i))[0] = hmap.get(trainData.get(j).get(i))[0] + 1;
					}
				} else if (trainData.get(j).get(trainData.get(0).size()-1).equalsIgnoreCase("1")) {
					if (hmap.get(trainData.get(j).get(i)) == null) {
						hmap.put(trainData.get(j).get(i), new int[] { 0, 1 });
					} else {
						hmap.get(trainData.get(j).get(i))[1] = hmap.get(trainData.get(j).get(i))[1] + 1;
					}
				}

			}
			features.add(hmap);
		}

		ArrayList<HashMap<String, double[]>> evidences = new ArrayList<HashMap<String, double[]>>();

		for (int i = 0; i < features.size() - 1; i++) {
			HashMap<String, double[]> hmap = new HashMap<String, double[]>();
			for (String x : features.get(i).keySet()) {
				hmap.put(x, new double[] { (double) features.get(i).get(x)[0] / features.get(features.size()-1).get("0")[0],
						(double) features.get(i).get(x)[1] / features.get(features.size()-1).get("1")[1] });
			}
			evidences.add(hmap);
		}

		HashMap<String, double[]> h = new HashMap<String, double[]>();
		double fal = features.get(features.size()-1).get("0")[0];
		double tru = features.get(features.size()-1).get("1")[1];
		h.put("0", new double[] { (double) fal / (fal + tru), 0 });
		h.put("1", new double[] { 0, (double) tru / (fal + tru) });
		evidences.add(h);
		printMFile(evidences, trainData, mfile);
		testAgainstAndPrintRFile(evidences, testData, rfile);
	}

	public void printMFile(ArrayList<HashMap<String, double[]>> evidences, ArrayList<ArrayList<String>> trainData,
			String mfile) throws Exception {
		FileWriter fm = new FileWriter(mfile);
		BufferedWriter bw = new BufferedWriter(fm);
		for (int i = 0; i < evidences.size() - 1; i++) {
			bw.write("Evidence : " + trainData.get(0).get(i) + '\n');
			bw.write("------------------------------------\n");
			for (String x : evidences.get(i).keySet()) {
				bw.write("P(" + trainData.get(0).get(i) + " = " + x + " | Class = 0) =  " + evidences.get(i).get(x)[0]
						+ '\n');
				bw.write("P(" + trainData.get(0).get(i) + " = " + x + " | Class = 1) =  " + evidences.get(i).get(x)[1]
						+ '\n');
				bw.write('\n');
			}

			bw.write('\n');
			bw.write('\n');
		}
		bw.close();
		fm.close();

	}

	public void testAgainstAndPrintRFile(ArrayList<HashMap<String, double[]>> evidences,
			ArrayList<ArrayList<String>> testData, String rfile) throws Exception {
		testData.get(0).add("predicted class");
		for (int i = 1; i < testData.size(); i++) {
			double truPredict = 1.0;
			double falPredict = 1.0;
			for (int j = 0; j < evidences.size() - 1; j++) {
				falPredict = falPredict * evidences.get(j).get(testData.get(i).get(j))[0];
				truPredict = truPredict * evidences.get(j).get(testData.get(i).get(j))[1];

			}

			falPredict = falPredict * evidences.get(evidences.size()-1).get("0")[0];
			truPredict = truPredict * evidences.get(evidences.size()-1).get("1")[1];

			double tot = (falPredict + truPredict);

			falPredict = falPredict / tot;
			truPredict = truPredict / tot;

			if (falPredict > truPredict) {
				testData.get(i).add("0");
			} else {
				testData.get(i).add("1");
			}

		}

		int confusionMatrix[] = new int[4];

		FileWriter fm = new FileWriter(rfile);
		BufferedWriter bw = new BufferedWriter(fm);

		bw.write("\n Row Wise Results \n");

		bw.write("Row   Actual Class   Predicted Class");
		bw.write('\n');
		for (int i = 1; i < testData.size(); i++) {

			bw.write("  " + i + "          " + testData.get(i).get(testData.get(0).size()-2) + "        " + testData.get(i).get(testData.get(0).size()-1));
			bw.write('\n');

			if (testData.get(i).get(testData.get(0).size()-2).equalsIgnoreCase("1") && testData.get(i).get(testData.get(0).size()-1).equalsIgnoreCase("1"))
				confusionMatrix[0]++;
			else if (testData.get(i).get(testData.get(0).size()-2).equalsIgnoreCase("0") && testData.get(i).get(testData.get(0).size()-1).equalsIgnoreCase("0")) {
				confusionMatrix[3]++;
			} else if (testData.get(i).get(testData.get(0).size()-2).equalsIgnoreCase("0") && testData.get(i).get(testData.get(0).size()-1).equalsIgnoreCase("1")) {
				confusionMatrix[1]++;
			} else if (testData.get(i).get(testData.get(0).size()-2).equalsIgnoreCase("1") && testData.get(i).get(testData.get(0).size()-1).equalsIgnoreCase("0")) {
				confusionMatrix[2]++;
			}

		}

		bw.write("\n\n     Confusion Matrix   \n");
		bw.write(" --------------------------------------------\n");
		bw.write("| True Positives = " + confusionMatrix[0] + " | False Positives = " + confusionMatrix[1] + " |\n");
		bw.write("---------------------------------------\n");
		bw.write("| False Negatives = " + confusionMatrix[2] + " | True Negatives = " + confusionMatrix[3] + " |\n");
		bw.write(" --------------------------------------------\n");

		bw.close();
		fm.close();

	}

}