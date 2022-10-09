import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

/** 
 *  This program takes a number of text files and filters them. The program
 *  then takes the data from the filter and inputs into a histogram. 
 *  Author: Ahmed Hashi
 *  JHED: ahashi1
 *  Date: 07/29/2022
 */

public class Proj3 {
   
   /**
    * The main method.
    * @param args commandline args
    */
   public static void main(String[] args) throws IOException {
      Scanner kb = new Scanner(System.in);
      char[] trimChars = {'.', '(', '"', '\''};
      char[] trimChars2 = {',', '.', '"', '!', ':', ')', '\''};
   
      String[] spam = loadWordsFromFile("spam.txt");
      String[] stopWords = loadWordsFromFile("stop_words.txt");
   
      String[] sanitizedSpam = getSanitizedWordArray(spam, stopWords);
      int[] spamHistogram = buildHistogram(sanitizedSpam);
      double[] normalizedSpamHistogram = normalizeHistogram(spamHistogram);
      String[] ham = loadWordsFromFile("ham.txt");
      String[] sanitizedHam = getSanitizedWordArray(ham, stopWords);
      int[] hamHistogram = buildHistogram(sanitizedHam);
      double[] normalizedHamHistogram = normalizeHistogram(hamHistogram);
   
      System.out.println("-----------------");
      String sms = kb.nextLine();
   
      double p = classifySMS(sms, normalizedHamHistogram, 
         normalizedSpamHistogram, stopWords);
      System.out.println("SMS: " + sms);
      System.out.println("score: " + p);
      
      if (p > 0) {
         System.out.println("class: HAM");
      }
      
      else {
         System.out.println("class: SPAM");
      }
      
      System.out.println("-------------------");
      
      System.out.println("Enter a text message:");
      String userSelect = kb.next();
       
      if (userSelect == ("Exit") || userSelect == ("exit")) {
         
         System.out.println("Bye!");
         System.exit(0);
         
      }
   
   
   }

   /**
    * This method classifies and SMS, i.e. it determines if a
    * given text message is spam or ham.
    *
    * @param smsText the SMS
    * @param normalizedHamHistogram the normalized histogram of the words 
    * in the ham dataset
    * @param normalizedSpamHistogram the normalized histogram of the words 
    * in the spam dataset
    * @param stopWords a String array with the stop words, loaded from the 
    * file stop_words.txt
    * @return the score of the SMS, it should be positive if it is HAM and 
    * negative if it is SPAM
    */
   public static double classifySMS(String smsText, 
                                    double[] normalizedHamHistogram, 
                                    double[] normalizedSpamHistogram,
                                    String[] stopWords) {
      String[] words = smsText.split(" ");
      String[] swords = getSanitizedWordArray(words, stopWords);
      double total = 0.0;
      for (int i = 0; i < swords.length; i++) {
         String word = swords[i];
         int hashCode = 0;
         if (isNumber(word)) {
            hashCode = 0;
         } else if (isMoney(word)) {
            hashCode = 1;
         } else if (isURL(word)) {
            hashCode = 2;
         } else {
            hashCode = 3 + (getHashCode(word) % 997);
         }

         total = total + Math.log(normalizedHamHistogram[hashCode] / 
                                  normalizedSpamHistogram[hashCode]);
      }
      return total;
   }
  

   /**
    * This method normalizes the histogram in such a way that it has unit
    * length.
    * @param histogram the input histogram
    * @return the unit normalized histogram
    */
   public static double[] normalizeHistogram(int[] histogram) {
      double[] output = new double[histogram.length];
      double norm = 0.0;
      for (int i = 0; i < histogram.length; i++) {
         norm = norm + histogram[i] * histogram[i];
      }
      norm = Math.sqrt(norm);
      for (int i = 0; i < histogram.length; i++) {
         output[i] = histogram[i] / norm;
      }
      return output;
   }

   /**
    * This method receives an array and copies it into another array that
    * is n times larger than the original array.
    * 
    * @param input the input String array
    * @param n make the output array n times larger than the input array
    * @return the output, enlarged array
    */
   public static String[] increaseArraySize(String[] input, int n) {
      String[] output = new String[100 * n];
      int indx;
      for (indx = 0; indx < input.length; ++indx) {
         output[indx] = input[indx];
      }
      return output;
   }

   /**
    * This method receives an array and copies the first size elements into
    * an output array and discards all the elements after size. 
    *
    * @param input the input String array
    * @param size the number of elements to keep
    * @return the output, trimmed array
    */
   public static String[] trimArray(String[] input, int size) {
      String[] output = new String[size];
      for (int i = 0; i <= size - 1; ++i) {
         output[i] = input[i];
      }
      return output;
   }

   /**
    * This method reads the filename line by line and calls split(" ") on 
    * each line. It then includes each word reported by split into an array. 
    * Must return an array with all the words. This method does not know ahead
    * of time how many words are present in the file and may not open and read
    * the file two times.
    *
    * @param filename the filename to load
    * @return a String array with all the words loaded from the file
    */
    
   public static String[] loadWordsFromFile(String filename) 
      throws FileNotFoundException {
      String[] words = new String[100];
      
      int track = 0;
      
      File randFile = new File(filename);
      Scanner scnr = new Scanner(randFile);
   
      while (scnr.hasNextLine()) {
         String content = scnr.nextLine();
         String[] newArray = content.split(" ");
         for (int i = 0; i < newArray.length; ++i) {
            track++;

            if (track > words.length) {
               increaseArraySize(words, 2);
            }
            else {
               words[i] = newArray[i];
            }
         }
      }
      return words;
   }

   /**
    * This method removes (trims) all the occurences of trimChars from the 
    * left of the string. For example if trimChars is {'(', '*'}:
    *
    * "(*(*(((75": returns "75"
    * "*(something()something)": returns "something()something)"
    * "99.95": returns "99.95"
    * "((": returns ""
    * "": returns ""
    *
    * Also, don't forget that the implementation fo this method must be
    * recursive.
    *
    * @param word the word
    * @param trimChars the characters to trim
    * @return the left trimmed word
    */
   
   
   public static String leftTrim(String word, char[] trimChars) {
      
      for (int i = 0; i < word.length(); ++i) {
         for (int j = 0; j < trimChars.length; ++j) {
            if (word.charAt(i) == trimChars[j]) {
               word = word.replace(trimChars[j], ' '); 
            }
         }
         
      }
      
      return word;
      
      
   }
   
   
   /**
    * This method reverses a string.
    *
    * Examples:
    * "Happy": returns "yppaH"
    * "123": returns "321"
    * "radar": returns "radar"
    * "": returns ""
    *
    * @param word the word
    * @return the reversed word
    */
    
   public static String reverse(String word) {
   
      char[] letters = new char[word.length()];
   
      int letterIndx = 0;
   
      for (int i = word.length() - 1; i >= 0; --i) {
         letters[letterIndx] = word.charAt(i);
         letterIndx++;
      }
   
      String empty = "";
      for (int i = 0; i < word.length(); i++) {
         empty = empty + letters[i];
      }
   
      return empty;
   }

   /**
    * Same as leftTrim but trims on the right of the string. This method 
    * should work without modification.
    *
    * @param word the word
    * @param trimChars the characters to trim
    * @return the right trimmed word
    */
   
   public static String rightTrim(String word, char[] trimChars) {
      return reverse(leftTrim(reverse(word), trimChars));
   }
   /**
    * This method searches for item in the array and returns the index
    * in which it finds it, or -1 if item is not in the array.
    *
    * @param array the array to be searched
    * @param item the item to search for
    * @return the lowest index in which appears in the array or -1 if item is 
    * not in the array
    */
    
   public static int getArrayIndexForItem(String[] array, String item) {
      for (int i = 0; i < array.length; ++i) {
         if (array[i].equals("item")) {
            return i;
         }
      }
      return -1;
   }
   

   /**
    * This method sanitizes the words array. It goes word by word and left
    * trims the following characters {'.', '(', '"', '\'' }, and right trims
    * the following characters {',', '.', '?', '!', ':', ')', '"', '\'' } from
    * each word. If after trimming the word has length greater than 0 and is
    * not present in the array stopWords then it needs to be included in the
    * return array, otherwise the word is dropped from further consideration.

    * @param words is the input unsanitized array of words
    * @param stopWords is the array with stop words
    * @return the sanitized array constructed as described aboce
    */
    
   public static String[] getSanitizedWordArray(String[] words, 
                                                 String[] stopWords) {
      String[] output = new String[words.length];
      
      char[] trimChars = {'.', '(', '"', '\''};
      char[] trimChars2 = {',', '.', '"', '!', ':', ')', '\''};
 
      for (int i = 0; i < words.length; ++i) {
         if (words[i] != null) {
            leftTrim(words[i], trimChars);
            rightTrim(words[i], trimChars2);
         
            if ((output.length > 0) && (!(words[i].equals(stopWords[i])))) {
               output[i] = words[i];
            }
            else {
               break;
            }
         }
      }
      return output;
   }
   /**
    * This method determines if a given word is a number. For the project 
    * we define number as any word that only contains the symbols: '0', '1'
    * '2', '3', '4', '5', '6', '7', '8, '9', '.', '-'
    * Examples:
    * "75": number
    * "75.54": number
    * "$99.95": not number
    * "-33.2": number
    * "45F": not number
    *
    * @param word the word
    * @return true if the input word is an amount of money, false if not
    */
    
   public static boolean isNumber(String word) {
      if (word == null) {
         return false;
      }
   
      for (int i = 0; i < word.length(); ++i) {
         if (!Character.isDigit(word.charAt(i))) {
            return false;
         }
      }
      return true;
   }
   
   /**
    * This method determines if a given word is an amount of money. For the 
    * project we define money as any word that contains the symbols: '�' or '$'.
    * Please use '\u00A3' instead of '�' for the autograder to work, this is 
    * limitation of Gradescope.
    * 
    * Examples:
    * "75": not money
    * "75bucks": not money
    * "$99.95": money
    * "$$$": money
    *
    * @param word the word
    * @return true if the input word is an amount of money, false if not
    */
   public static boolean isMoney(String word) {
      if (word == null) {
         return false;
      }
   
      for (int i = 0; i < word.length(); ++i) {
         if (word.charAt(i) == '$' || word.charAt(i) == '\u00A3') {
            return true;
         }
      }
      return false;
   }

   /**
    * This method determines if a given word is an URL. For the project we 
    * define URL as a word that has at least one dot and no pair of 
    * consecutive dots. Examples:
    * "my...friend": not a URL
    * "my...F.r.i.e.n.d": not a URL
    * "cs.jhu.edu": a URL
    * "GET.OUT.OF.HERE": a URL
    *
    * @param word the word
    * @return true if the input word is a URL, false if not
    */
   public static boolean isURL(String word) {
   
      if (word == null) {
         return false;
      }

      for (int i = 0; i < word.length(); ++i) {
         if (word.charAt(i) == '.' && word.charAt(i - 1) == '.') {
            return false;
         }
      }
   
      for (int i = 0; i < word.length(); ++i) {
         if (word.charAt(i) == '.') {
         }
      }
      return true;
   }
   /**
    * This method receives a word and computes its non-negative hash code. 
    * Observe that the hash code returned by this method can be from 0 to the 
    * maximum integer in Java, but for a fixed word the hash code is always 
    * the same.
    *
    * @param word the word
    * @return the hashcode
    */
    
   public static int getHashCode(String word) {
      int hashCode = word.hashCode() & 0xfffffff;
      return hashCode;
   }

   /**
    * This method receives an array of words (strings) and builds a histogram.
    * The histogram is 1000-dimensional and will be structured as follows:
    *
    * - index 0 contains the count of words in the array for which isNumber is
    * true
    * - index 1 contains the count of words in the array for which isMoney is
    * true
    * - index 2 contains the count of words in the array for which isURL is
    * true
    * - from index 3 to index 999 if the word is not a number, money or URL, 
    * then a hash code of the word (from 0 to 996 will) be computed and will 
    * be counted in index 3 to 999 of the histogram
    *
    * @param words is the array of words
    * @return will return a 1000-dimensional histogram as described above
    */


   public static int[] buildHistogram(String[] words) {
      int[] histogram = new int[1000];

      for (String i: words) {
         if (i == null) {
            continue;
         }
         if (isNumber(i)) {
            histogram[0]++;
         }
         else if (isMoney(i)) {
            histogram[1]++;
         }
         else if (isURL(i)) {
            histogram[2]++;
         }
         else {
            histogram[(getHashCode(i) % 997) + 3]++;
         }
      }
      histogram = smoothHistogram(histogram);
      return histogram;
   }
   
   /**
   * This method receives and smoothes a histogram.
   * @param histogram is the integer array found after reading files
   * @return the histogram
   */
   public static int[] smoothHistogram(int[] histogram) {
      for (int i = 0; i < 1000; ++i) {
         if (histogram[i] == 0) {
            histogram[i] = 1;
         }
         else {
            break;
         }
      }
      return histogram;
   }
}
 
