import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;

public class Ranker {
    
    public static final double title_score = 15;
    public static final double h1_score = 6;
    public static final double h2_score = 5;
    public static final double h3_score = 4;
    public static final double h4_score = 3;
    public static final double h5_score = 2;
    public static final double h6_score = 1.5;
    public static final double restOfTags_score = 1;
    public static final double maxDistance = 12000.0;
    private static ArrayList<Double>tagScores;
    private static HashMap<String,Double> extensionsDistance;
    private static final String extensions_file = "src/main/java/extensions.txt";
    private static LocalDate start_date = null;
    private static HashMap<String,Integer> months = null;

    static {
        start_date = LocalDate.of(1990, 1, 1);
        months = new HashMap<>();
        months.put("jan", 1);
        months.put("feb", 2);
        months.put("mar", 3);
        months.put("apr", 4);
        months.put("may", 5);
        months.put("jun", 6);
        months.put("jul", 7);
        months.put("aug", 8);
        months.put("sep", 9);
        months.put("sept", 9);
        months.put("oct", 10);
        months.put("nov", 11);
        months.put("dec", 12);

        tagScores = new ArrayList<Double>();
        tagScores.add(title_score);
        tagScores.add(h1_score);
        tagScores.add(h2_score);
        tagScores.add(h3_score);
        tagScores.add(h4_score);
        tagScores.add(h5_score);
        tagScores.add(h6_score);
        tagScores.add(restOfTags_score);
        tagScores.add(restOfTags_score);
        tagScores.add(restOfTags_score);
        tagScores.add(restOfTags_score);
        tagScores.add(restOfTags_score);
        extensionsDistance = new HashMap<String,Double>();
        readExtensions();
    }
    /**
     * this function reads extensions and stores its distance from egypt
     */
    private static void readExtensions()
    {
        File file;
        Scanner myReader;
        try {
            file = new File(extensions_file);
            myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                String[] splitted = line.split(" ",2);
                extensionsDistance.put(splitted[0],Double.parseDouble(splitted[1]));
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while reading from file.");
            e.printStackTrace();
        }
    }

    /**
     * this function loops through the tags and calculate each word's score
     * @param listOfWords: all tags with their text
     * @return return map of words score in the processed document(url)
     */
    public static HashMap<String,Double> CalculateWordScore(ArrayList<ArrayList<String>> listOfWords, int totalNumOfWords)
    {
        HashMap<String,Double> wordScore = new HashMap<String, Double>();
        int idx = 0;
        for (ArrayList<String> listOfWord : listOfWords) {
            for (String s : listOfWord) {
                if(wordScore.containsKey(s))
                    wordScore.put(s,wordScore.get(s)+ tagScores.get(idx));
                else
                    wordScore.put(s, tagScores.get(idx));
            }
            idx++;
        }
        for (Entry<String, Double> entry : wordScore.entrySet()) {
            entry.setValue(entry.getValue()/totalNumOfWords);
        }
        return wordScore;
    }

    /**
     * this function calculates the geographic location's score of a given url
     * @param url:the processed url
     * @return the score of geographic location of the processed url
     */
    public static double CalculateGeographicLocationScore(String url)
    {
        int start_idx = url.lastIndexOf('.');
        int end_idx = url.indexOf('/', start_idx);
        if(end_idx == -1)
            end_idx = url.length();
        String ext = url.substring(start_idx , end_idx);
        if(extensionsDistance.containsKey(ext))
            return 1.0-(extensionsDistance.get(ext)/maxDistance);
        return 0.0;
    }

    /**
     * this function calculates date score
     * @param date: last modified date of url
     * @return date score
     */
    public static double CalculateDateScore(String date)
    {
        LocalDate current_date = LocalDate.now();
        String [] data = date.split(" ");
        LocalDate url_date = LocalDate.of(Integer.parseInt(data[2]), months.get(data[1].toLowerCase()), Integer.parseInt(data[0]));
        long DaysInBetween = ChronoUnit.DAYS.between(start_date, url_date);
        long TotalDays = ChronoUnit.DAYS.between(start_date, current_date);
        return (DaysInBetween*1.0/TotalDays);
    }
}