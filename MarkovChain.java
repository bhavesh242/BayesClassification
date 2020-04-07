import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

public class MarkovChain {

	public static void main(String[] args) throws Exception {

		if (args.length != 3) {
			System.out.println("Please Provide 3 arguments in the following order :");
			System.out.println("AUTH-DIR/ PROB-FILE RESULT-FILE");
			System.exit(0);
		}

		MarkovChainBuilder1 mcb = new MarkovChainBuilder1();
		mcb.buildAndPrint(args[0], args[1], args[2]);
	}
}

class MarkovChainBuilder1 {

	public void buildAndPrint(String dir, String prob, String result) throws Exception {
		String line = null;
		String filePath = "./" + dir;
		File folder = new File(filePath);
		ArrayList<String> words = new ArrayList<String>();
		for (File file : folder.listFiles()) {

			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			while ((line = br.readLine()) != null) {
				StringBuffer s = new StringBuffer();
				for (int i = 0; i < line.length(); i++) {
					if (line.charAt(i) >= 'a' && line.charAt(i) <= 'z'
							|| line.charAt(i) >= 'A' && line.charAt(i) <= 'Z') {
						s.append(line.charAt(i));
					} else {
						if (s.length() != 0) {
							words.add(new String(s));
							s.setLength(0);
						}
					}
					if (i == line.length() - 1) {
						words.add(new String(s));
					}
				}
			}

			br.close();
			fr.close();
		}

		ArrayList<String> stopWords = new ArrayList<String>();
		String file2 = "./EnglishStopwords.txt";
		FileReader fr2 = new FileReader(file2);
		BufferedReader br2 = new BufferedReader(fr2);
		while ((line = br2.readLine()) != null) {
			stopWords.add(line.toLowerCase());
		}

		ArrayList<String> cleanUp = new ArrayList<String>();
		for (int i = 0; i < words.size(); i++) {
			if (words.get(i).length() != 0 && !words.get(i).equalsIgnoreCase("")) {
				if (!stopWords.contains(words.get(i).toLowerCase())) {
					cleanUp.add(words.get(i).toLowerCase());
				}
			}
		}

		LinkedHashMap<String, Integer> unigrams = new LinkedHashMap<String, Integer>();
		for (int i = 0; i < cleanUp.size(); i++) {
			unigrams.put(cleanUp.get(i), unigrams.getOrDefault(cleanUp.get(i), 0) + 1);
		}

		LinkedHashMap<String, Integer> bigrams = new LinkedHashMap<String, Integer>();
		for (int i = 0; i < cleanUp.size() - 1; i++) {
			bigrams.put(cleanUp.get(i) + " " + cleanUp.get(i + 1),
					bigrams.getOrDefault(cleanUp.get(i) + " " + cleanUp.get(i + 1), 0) + 1);
		}

		LinkedHashMap<String, Integer> trigrams = new LinkedHashMap<String, Integer>();
		for (int i = 0; i < cleanUp.size() - 2; i++) {
			trigrams.put(cleanUp.get(i) + " " + cleanUp.get(i + 1) + " " + cleanUp.get(i + 2),
					trigrams.getOrDefault(cleanUp.get(i) + " " + cleanUp.get(i + 1) + " " + cleanUp.get(i + 2), 0) + 1);
		}

		LinkedHashMap<String, Double> uniProb = new LinkedHashMap<String, Double>();
		LinkedHashMap<String, Double> biProb = new LinkedHashMap<String, Double>();
		LinkedHashMap<String, Double> triProb = new LinkedHashMap<String, Double>();

		for (String x : unigrams.keySet()) {
			uniProb.put(x, (double) unigrams.get(x) / cleanUp.size());
		}
		for (String x : bigrams.keySet()) {
			String t[] = x.split(" ");
			biProb.put(x, (double) bigrams.get(x) / unigrams.get(t[0]));
		}
		for (String x : trigrams.keySet()) {
			String t[] = x.split(" ");
			triProb.put(x, (double) trigrams.get(x) / bigrams.get(t[0] + " " + t[1]));
		}

		FileWriter fw = new FileWriter("./" + prob);
		BufferedWriter bw = new BufferedWriter(fw);
		for (String x : uniProb.keySet()) {
			bw.write("P(" + x + ") = " + uniProb.get(x) + '\n');
		}

		for (String x : bigrams.keySet()) {
			String t[] = x.split(" ");
			bw.write("P(" + t[1] + " | " + t[0] + ") = " + biProb.get(x) + '\n');

		}

		for (String x : trigrams.keySet()) {
			String t[] = x.split(" ");
			bw.write("P(" + t[2] + " | " + t[0] + " " + t[1] + ") = " + triProb.get(x) + '\n');

		}
		bw.close();
		fw.close();

		ArrayList<Integer> uniqueGrams = new ArrayList<Integer>();
		Random r = new Random();
		int count = 0;
		while (count < 10) {
			int rand = r.nextInt(uniProb.size());
			if (!uniqueGrams.contains(count)) {
				uniqueGrams.add(rand);
				count++;
			}
		}

		ArrayList<Sequences> sentence = new ArrayList<Sequences>();
		for (int i = 0; i < count; i++) {
			int t = uniqueGrams.get(i);
			for (String x : uniProb.keySet()) {
				if (t == 0) {
					sentence.add(new Sequences(x, uniProb.get(x)));
					break;
				} else {
					t--;
				}
			}
		}

		for (int i = 0; i < sentence.size(); i++) {
			String p0 = sentence.get(i).seq;
			ArrayList<Sequences> biGr = new ArrayList<Sequences>();
			for (String x : biProb.keySet()) {
				String p = x.split(" ")[0];
				if (p.equalsIgnoreCase(p0)) {
					biGr.add(new Sequences(x, biProb.get(x)));
				}
			}

			double ran = Math.random();
			int j;
			for (j = 0; j < biGr.size(); j++) {
				ran = ran - biGr.get(j).prob;
				if (ran <= 0.0)
					break;
			}

			sentence.get(i).seq = biGr.get(j).seq;
			sentence.get(i).prob = sentence.get(i).prob * biGr.get(j).prob;

		}

		for (int i = 0; i < sentence.size(); i++) {

			for (int wordCount = 2; wordCount < 20; wordCount++) {
				String t[] = sentence.get(i).seq.split(" ");
				String t0 = t[t.length - 2];
				String t1 = t[t.length - 1];
				ArrayList<Sequences> triGr = new ArrayList<Sequences>();
				for (String key : triProb.keySet()) {
					String k[] = key.split(" ");
					if (k[0].equalsIgnoreCase(t0) && k[1].equalsIgnoreCase(t1)) {
						triGr.add(new Sequences(key, triProb.get(key)));
					}
				}

				double ran = Math.random();
				int j;
				for (j = 0; j < triGr.size(); j++) {
					ran = ran - triGr.get(j).prob;
					if (ran <= 0.0)
						break;
				}

				sentence.get(i).seq = sentence.get(i).seq + " " + triGr.get(j).seq.split(" ")[2];
				sentence.get(i).prob = sentence.get(i).prob * triGr.get(j).prob;
			}
		}

		FileWriter fwr = new FileWriter("./" + result);
		BufferedWriter bwr = new BufferedWriter(fwr);
		for (int i = 0; i < count; i++) {
			bwr.write("Sequence " + (i + 1) + " :\n");
			bwr.write(sentence.get(i).seq + "\n");
			bwr.write("Probability : " + sentence.get(i).prob + "\n\n");
		}

		bwr.close();
		fwr.close();

	}
}

class Sequences {
	String seq;
	Double prob;

	Sequences(String seq, Double prob) {
		this.seq = seq;
		this.prob = prob;
	}
}
