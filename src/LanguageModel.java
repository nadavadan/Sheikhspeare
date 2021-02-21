/*
Assignment number : 9.2
File Name : LanguageModel.java
Name : nadav abutbul
Student ID : 204328116
Email : nadav.abutbul@post.idc.ac.il
*/
import std.StdIn;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class LanguageModel {

	// The length of the moving window
	private int windowLength; 
	
	// The map for managing the (window, LinkedList) mappings 
	private HashMap<String, LinkedList<CharProb>> probabilities;

	// Random number generator:
	// Used by the getRandomChar method, initialized by the class constructors. 
	Random randomGenerator;
	
	/**
	 * Creates a new language model, using the given window length
	 * and a given (fixed) number generator seed.
	 * @param windowLength
	 * @param seed
	 */
	public LanguageModel(int windowLength, int seed) {
		this.randomGenerator = new Random(seed);
		this.windowLength = windowLength;
		probabilities = new HashMap<String, LinkedList<CharProb>>();
	}	
	
	/**
	 * Creates a new language model, using the given window length
	 * and a random number generator seed.
	 * @param windowLength
	 */
	public LanguageModel(int windowLength) {
		this.randomGenerator = new Random();
		this.windowLength = windowLength;
		probabilities = new HashMap<String, LinkedList<CharProb>>();
	}

	/**
	 * Builds a language model from the text in standard input (the corpus).
	 */
	public void train() {
		String window ="";
		char c;
		for (int i = 0; i < windowLength; i++){
			window = window + StdIn.readChar();

		}
		while (!StdIn.isEmpty()){
			c = StdIn.readChar();
			//LinkedList<CharProb> probs = this.probabilities.get(window);
			if (probabilities.get(window) == null){
				LinkedList<CharProb> probs = new LinkedList<CharProb>();
				probabilities.put(window, probs);
			}
			calculateCounts(probabilities.get(window), c);

			window += c;
			window = window.substring(1);
		}
		for (LinkedList<CharProb> probs : probabilities.values()){
			calculateProbabilities(probs);
		}
	}

	// Calculates the counts of the current character.
	private void calculateCounts(LinkedList<CharProb> probs, char c) {
		int counter = 0;
		for (int i = 0; i < probs.size(); i++) {
			if (probs.get(i).chr == c) {
				probs.get(i).count++;
				counter++;
			}
		}
		if (counter == 0) {
			probs.add(new CharProb(c));
		}

	}

	// Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	private void calculateProbabilities(LinkedList<CharProb> probs) {
		int numOfChars = 0;
		for (int i = 0; i < probs.size(); i++){
			numOfChars += probs.get(i).count;

		}
		probs.get(0).p = (double) probs.get(0).count/numOfChars;
		probs.get(0).cp = probs.get(0).p;
		for (int j = 1; j < probs.size(); j++){
			probs.get(j).p = (double) probs.get(j).count/numOfChars;
		    probs.get(j).cp = probs.get(j-1).cp + probs.get(j).p;
		}
	}


	/**
	 * Returns a string representing the probabilities map.
	 */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : probabilities.keySet()) {
			LinkedList<CharProb> keyProbs = probabilities.get(key);
			str.append(key + " : " +  toString(keyProbs));
			str.append("\n");
		}
		return str.toString();
	}
	// Returns a textual representation of the given probabilities list, using the
	// format ((c n),(c n),...,(c n)) where c is a character and n is a count value.
	public static String toString(LinkedList<CharProb> probs) {
		String problist = "(";
		for (int i = 0; i < probs.size(); i++){
			problist += probs.get(i).toString();
		}
		problist += ")";
		return problist;
	}

	/**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start. 
	 * @param textLength - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		String window;
		String finalStory = initialText;
		if (windowLength > initialText.length()){
			return  initialText;
		}
		else if (windowLength == initialText.length()){
			window = initialText;
		}
		else{
			window = initialText.substring(initialText.length() - windowLength);
		}
		for (int i = 0; i < textLength - windowLength; i++){
			char randomC = getRandomChar(this.probabilities.get(window));
			window += randomC;
			window = window.substring(1);
			finalStory += randomC;
		}
		return finalStory;
	}

	// Returns a random character from the given probabilities list.
	public char getRandomChar(LinkedList<CharProb> probs) {
		calculateProbabilities(probs);
		double random = randomGenerator.nextDouble();
		for (int i = 0; i < probs.size(); i++){
			if (probs.get(i).cp >= random){
				return probs.get(i).chr;
			}
		}
		return probs.get(0).chr;
	}


	/**
	 * A Test of the LanguageModel class.
	 * Learns a given corpus (text file) and generates text from it.
	 */
	public static void main(String []args) {
		StdIn.setInput("/home/liron/IdeaProjects/HW09/shakespeareinlove.txt");
		int windowLength = Integer.parseInt(args[0]);  // window length
		String initialText = args[1];			      // initial text
		int textLength = Integer.parseInt(args[2]);	  // size of generated text
		boolean random = (args[3].equals("random") ? true : false);  // random / fixed seed
		LanguageModel lm;

		// Creates a language model with the given window length and random/fixed seed
		if (random) {
			// the generate method will use a random seed
			lm = new LanguageModel(windowLength);      
		} else {
			// the generate method will use a fixed seed = 20 (for testing purposes)
			lm = new LanguageModel(windowLength, 20); 
		}
		
		// Trains the model, creating the map.
		lm.train();
		
		// Generates text, and prints it.
		System.out.println(lm.generate(initialText,textLength));
	}
}
