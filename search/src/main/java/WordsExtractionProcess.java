
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * This class handles Parsing HTML files and perform pre processing on words
 **/
public class WordsExtractionProcess {

    public static HashSet<String>stoppingWordsList;

    static {
        stoppingWordsList = new HashSet<>();
        loadStoppingWords("src/main/java/StoppingWords.txt");
    }
    
    /**
     * This function remove un related words and parts from words that are un related and ignore other lang than english
     **/
    static String RemoveUnrelated(String word){
        word=word.replaceAll("\\s*\\[[^\\]]*\\]\\s*", ""); //removing unrelated numbers from brackets
        word=word.replaceAll("\\s*\\{[^\\}]*\\}\\s*", " ");//removing unrelated from pranthes
        word=word.replace(".",""); // remove full stop
        word=word.replace(",",""); // remove comma
        word=word.replace("[",""); // remove square brackets
        word=word.replace("]",""); // remove
        word=word.replace("(",""); // remove bracket
        word=word.replace(")",""); // remove
        word=word.replace("'",""); // remove single qoute
        word=word.replace(",",""); // remove comma
        word = word.replaceAll("^\"|\"$", ""); // remove double qoutes
        word=word.replace("#",""); // remove hash
        word=word.replace("!",""); // remove exclamation
        word=word.replace("@",""); // remove
        word=word.replace("$",""); // remove dollar
        word=word.replace("%",""); // remove percentage
        word=word.replace("&",""); // remove amberssand
        word=word.replace(";",""); // remove semicolon
        word=word.replace(":",""); // remove colon
        word=word.replace("-",""); // remove dash
        word=word.replace("_",""); // remove underscore
        word=word.replace("/",""); // remove slash

        //Reject other language other than english
        for(int i=0;i<word.length();i++){
            int ch=word.charAt(i);
            if(ch > 255){
                word=word.replace(String.valueOf(word.charAt(i)),""); // remove underscore
            }
        }
        return  word;
    }
    
    /**
     * This function split given string and return array list of split strings
     **/
    public static ArrayList<String> SplitStrings(String sentence){
        return new ArrayList<>(Arrays.asList(sentence.toLowerCase().split(" ")));
    }
    /**
     * This function take a path of file that contain list os stopping words to be removed
     **/
    private static void loadStoppingWords(String fileName){
        File file;
        Scanner myReader;
        try {
            file = new File(fileName);
            myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                stoppingWordsList.add(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while reading from file.");
            e.printStackTrace();
        }
    }
    /**
     * This function removes Stopping words from given array list
     **/
    public static ArrayList<String> RemovingStoppingWords(ArrayList<String> listOfWords) {

        for(int i=0;i<listOfWords.size();i++)
            listOfWords.set(i,RemoveUnrelated(listOfWords.get(i)));

        ArrayList<String>filteredWords=new ArrayList<>();
        for (String listOfWord : listOfWords) {
            if (listOfWord.length()!=0 &&(!stoppingWordsList.contains(listOfWord)))
                filteredWords.add(listOfWord);
        }
        return filteredWords;
    }
    /**
     * This function apply Stemming algorithm on array list of words
     **/
    public static ArrayList<String>ApplyingStemming(ArrayList<String> listOfWords){
        Stemmer s = new Stemmer();
        ArrayList<String>listOfStemmedWords=new ArrayList<String>();
        for (String listOfWord : listOfWords) listOfStemmedWords.add(s.Stemming(listOfWord));
        Set<String> set = new HashSet<>(listOfStemmedWords);
        listOfStemmedWords.clear();
        listOfStemmedWords.addAll(set);
        return  listOfStemmedWords;
    }

    static class Stemmer {
    
        private String word;
        private int i,
                j, k;
    
        /**
         * Constructor that take word and transform it to lower case
         **/
        public Stemmer() {
        }
        /**
         * This function takes a new word
         **/
        private void NewWord(String word){
            this.word=word.toLowerCase();
            i = word.length();
        }
    
        /**
         * This function check if there is a consonant char at given position
         **/
        private boolean isConsonant(int i) {
            switch (word.charAt(i)) {
                case 'a':
                case 'e':
                case 'i':
                case 'o':
                case 'u':
                    return false;
                case 'y':
                    return (i == 0) || !isConsonant(i - 1);
                default:
                    return true;
            }
        }
    
        /**
         *   This function measures the number of consonant sequences between 0 and j. if c is
         a consonant sequence and v a vowel sequence, and <..> indicates arbitrary
         presence,
         <c><v>       gives 0
         <c>vc<v>     gives 1
         <c>vcvc<v>   gives 2
         <c>vcvcvc<v> gives 3
         ....
         **/
        private int MeasureNumberOfConsonantSeq() {
            int n = 0;
            int i = 0;
            while (true) {
                if (i > j) return n;
                if (!isConsonant(i)) break;
                i++;
            }
            i++;
            while (true) {
                while (true) {
                    if (i > j) return n;
                    if (isConsonant(i)) break;
                    i++;
                }
                i++;
                n++;
                while (true) {
                    if (i > j) return n;
                    if (!isConsonant(i)) break;
                    i++;
                }
                i++;
            }
        }
    
        /**
         *  this function check if word contains a vowel
         **/
        private boolean ContainVowel() {
            int i;
            for (i = 0; i <= j; i++) if (!isConsonant(i)) return true;
            return false;
        }
    
        /**
         *  This function check if there is double Consonant or not in word
         **/
    
        private boolean ContainDoubleConsonant(int j) {
            if (j < 1) return false;
            if (word.charAt(j) != word.charAt(j - 1)) return false;
            return isConsonant(j);
        }
    
        /** function CVC is is checking this seq : consonant - vowel - consonant
         and also if the second c is not w,x or y. this is used when trying to
         restore an e at the end of a short word. e.g.
         cav(e), lov(e), hop(e), crim(e), but ,snow, box, tray.
         **/
    
        private boolean CVC(int i) {
            if (i < 2 || !isConsonant(i) || isConsonant(i - 1) || !isConsonant(i - 2)) return false;
            {
                int ch = word.charAt(i);
                return ch != 'w' && ch != 'x' && ch != 'y';
            }
        }
    
        /**
         * Get the subsequence of string at end of word
         **/
        private boolean ends(String s) {
            int l = s.length();
            int o = k - l + 1;
            if (o < 0) return false;
            for (int i = 0; i < l; i++) if (word.charAt(o + i) != s.charAt(i)) return false;
            j = k - l;
            return true;
        }
    
        /**
         * This function Replace ending of word
         **/
    
        private void SetTo(String s) {
            int l = s.length();
            int o = j + 1;
            for (int i = 0; i < l; i++)
                word = word.substring(0,o + i)+s.charAt(i)+ word.substring(o+i+1);
            k = j + l;
        }
    
        /**
         * This function checking measure of word to replace the ending of word
         **/
    
        private void ReplaceEnding(String s) {
            if (MeasureNumberOfConsonantSeq() > 0) SetTo(s);
        }
        /**
         * This function handle words ends with er,
         **/
        private void Step0(){
            if(word.charAt(word.length()-1)=='r'){
                if(word.charAt(word.length()-2)=='e')
                    k-=2;
            }
            else if(word.charAt(word.length()-1)=='y'){
                if(word.charAt(word.length()-2)=='l')
                    k-=2;
            }
            if(ends("ves")){
                k-=2;
                word=word.substring(0,k)+'f';
            }
        }
    
        /** step1() gets rid of plurals and -ed or -ing. e.g.
         caresses  ->  caress
         ponies    ->  poni
         ties      ->  ti
         caress    ->  caress
         cats      ->  cat
         feed      ->  feed
         agreed    ->  agree
         disabled  ->  disable
         matting   ->  mat
         mating    ->  mate
         meeting   ->  meet
         milling   ->  mill
         messing   ->  mess
         meetings  ->  meet
         **/
        private void Step1() {
            if (word.charAt(k) == 's') {
                if (ends("sses")) k -= 2;
                else if (ends("ies")) SetTo("i");
                else if (word.charAt(k - 1) != 's') k--;
            }
            if (ends("eed")) {
                if (MeasureNumberOfConsonantSeq() > 0) k--;
            } else if ((ends("ed") || ends("ing")) && ContainVowel()) {
                k = j;
                if (ends("at")) SetTo("ate");
                else if (ends("bl")) SetTo("ble");
                else if (ends("iz")) SetTo("ize");
                else if (ContainDoubleConsonant(k)) {
                    k--;
                    {
                        int ch = word.charAt(k);
                        if (ch == 'l' || ch == 's' || ch == 'z') k++;
                    }
                } else if (MeasureNumberOfConsonantSeq() == 1 && CVC(k)) SetTo("e");
            }
        }
    
        /**
         * Step2 turns terminal y to i when there is another vowel in the stem.
         **/
    
        private void Step2() {
            if (ends("y") && ContainVowel())
                word = word.substring(0,k)+'i'+ word.substring(k+1);// Need to be revised
        }
    
        /**
         * Step3 function maps double suffices to single ones. so -ization ( = -ize plus-ation) maps to -ize etc.
         * note that the string before the suffix must give m() > 0.
         **/
    
        private void Step3() {
            if (k == 0) return;
            switch (word.charAt(k - 1)) {
                case 'a':
                    if (ends("ational")) {
                        ReplaceEnding("ate");
                        break;
                    }
                    if (ends("tional")) {
                        ReplaceEnding("tion");
                        break;
                    }
                    break;
                case 'c':
                    if (ends("enci")) {
                        ReplaceEnding("ence");
                        break;
                    }
                    if (ends("anci")) {
                        ReplaceEnding("ance");
                        break;
                    }
                    break;
                case 'e':
                    if (ends("izer")) {
                        ReplaceEnding("ize");
                        break;
                    }
                    break;
                case 'l':
                    if (ends("bli")) {
                        ReplaceEnding("ble");
                        break;
                    }
                    if (ends("alli")) {
                        ReplaceEnding("al");
                        break;
                    }
                    if (ends("entli")) {
                        ReplaceEnding("ent");
                        break;
                    }
                    if (ends("eli")) {
                        ReplaceEnding("e");
                        break;
                    }
                    if (ends("ousli")) {
                        ReplaceEnding("ous");
                        break;
                    }
                    break;
                case 'o':
                    if (ends("ization")) {
                        ReplaceEnding("ize");
                        break;
                    }
                    if (ends("ation")) {
                        ReplaceEnding("ate");
                        break;
                    }
                    if (ends("ator")) {
                        ReplaceEnding("ate");
                        break;
                    }
                    break;
                case 's':
                    if (ends("alism")) {
                        ReplaceEnding("al");
                        break;
                    }
                    if (ends("iveness")) {
                        ReplaceEnding("ive");
                        break;
                    }
                    if (ends("fulness")) {
                        ReplaceEnding("ful");
                        break;
                    }
                    if (ends("ousness")) {
                        ReplaceEnding("ous");
                        break;
                    }
                    break;
                case 't':
                    if (ends("aliti")) {
                        ReplaceEnding("al");
                        break;
                    }
                    if (ends("iviti")) {
                        ReplaceEnding("ive");
                        break;
                    }
                    if (ends("biliti")) {
                        ReplaceEnding("ble");
                        break;
                    }
                    break;
                case 'g':
                    if (ends("logi")) {
                        ReplaceEnding("log");
                        break;
                    }
            }
        }
    
        /**
         *  Step4 deals with -ic-, -full, -ness etc. similar strategy to step3. *
         **/
    
        private void step4() {
            switch (word.charAt(k)) {
                case 'e':
                    if (ends("icate")) {
                        ReplaceEnding("ic");
                        break;
                    }
                    if (ends("ative")) {
                        ReplaceEnding("");
                        break;
                    }
                    if (ends("alize")) {
                        ReplaceEnding("al");
                        break;
                    }
                    break;
                case 'i':
                    if (ends("iciti")) {
                        ReplaceEnding("ic");
                        break;
                    }
                    break;
                case 'l':
                    if (ends("ical")) {
                        ReplaceEnding("ic");
                        break;
                    }
                    if (ends("ful")) {
                        ReplaceEnding("");
                        break;
                    }
                    break;
                case 's':
                    if (ends("ness")) {
                        ReplaceEnding("");
                        break;
                    }
                    break;
            }
        }
    
        /**
         *  Step5 takes off -ant, -ence etc., in context <c>vcvc<v>.
         **/
    
        private void Step5() {
            if (k == 0) return; /* for Bug 1 */
            switch (word.charAt(k - 1)) {
                case 'a':
                    if (ends("al")) break;
                    return;
                case 'c':
                    if (ends("ance")) break;
                    if (ends("ence")) break;
                    return;
                case 'e':
                    if (ends("er")) break;
                    return;
                case 'i':
                    if (ends("ic")) break;
                    return;
                case 'l':
                    if (ends("able")) break;
                    if (ends("ible")) break;
                    return;
                case 'n':
                    if (ends("ant")) break;
                    if (ends("ement")) break;
                    if (ends("ment")) break;
                    /* element etc. not stripped before the m */
                    if (ends("ent")) break;
                    return;
                case 'o':
                    if (ends("ion") && j >= 0 && (word.charAt(j) == 's' || word.charAt(j) == 't')) break;
                    /* j >= 0 fixes Bug 2 */
                    if (ends("ou")) break;
                    return;
                /* takes care of -ous */
                case 's':
                    if (ends("ism")) break;
                    return;
                case 't':
                    if (ends("ate")) break;
                    if (ends("iti")) break;
                    return;
                case 'u':
                    if (ends("ous")) break;
                    return;
                case 'v':
                    if (ends("ive")) break;
                    return;
                case 'z':
                    if (ends("ize")) break;
                    return;
                default:
                    return;
            }
            if (MeasureNumberOfConsonantSeq() > 1) k = j;
        }
    
        /**
         *  Step6 removes a final -e if m() > 1.
         **/
    
        private void step6() {
            j = k;
            if (word.charAt(k) == 'e') {
                int a = MeasureNumberOfConsonantSeq();
                if (a > 1 || a == 1 && !CVC(k - 1)) k--;
            }
            if (word.charAt(k) == 'l' && ContainDoubleConsonant(k) && MeasureNumberOfConsonantSeq() > 1) k--;
        }
    
        public String Stemming(String word) {
            NewWord(word);
            k = i - 1 ;
            if (k > 1) {
                Step0();
                Step1();
                Step2();
                Step3();
                step4();
                Step5();
                step6();
            }
            return this.word.substring(0,k+1);
        }
    }
}
