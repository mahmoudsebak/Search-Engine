
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;


public class IndexerDbAdapter {

    // contains 2 tables, one has columns: URL, content, page_rank
    // the other has columns: word, URL, tf, word_tag

    // these are the column names
    public static final String COL_ID = "_id";
    public static final String COL_URL = "url";
    // content of the url after removing tags (used in phrase search)
    public static final String COL_CONTENT = "content";
    public static final String COL_TITLE = "title";
    public static final String COL_CRAWLED_AT = "crawled_at";
    public static final String COL_INDEXED = "indexed";
    public static final String COL_PAGE_RANK = "page_rank";
    public static final String COL_DATE_SCORE = "date_score";
    public static final String COL_GEO_SCORE = "geographic_score";

    public static final String COL_WORD = "word";
    public static final String COL_STEM = "stem";
    public static final String COL_SCORE = "score";

    public static final String COL_SRC_URL = "src_url";
    public static final String COL_DST_URL = "dst_url";

    public static final String COL_IMAGE = "image";
    public static final String COL_ALT = "alt";

    public static final String COL_QUERY = "query";

    public static final String COL_FREQ = "freq";

    public static final String COL_PERSON = "person";
    public static final String COL_REGION = "region";
    public static final String COL_COUNT = "count";

    private Connection conn;
    private static final String DATABASE_NAME = "dba_search_indexer";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/";
    private static final String TABLE_URLS_NAME = "tb1_urls";
    private static final String TABLE_URLS_INDEX_NAME = "tbl_urls_index";
    private static final String TABLE_WORDS_NAME = "tb2_words";
    private static final String TABLE_WORDS_INDEX_NAME = "tb2_words_url_index";
    private static final String TABLE_WORDS_INDEX2_NAME = "tb2_stem_url_index";
    private static final String TABLE_LINKS_NAME = "tb3_links";
    private static final String TABLE_LINKS_INDEX_NAME = "tb3_links_index";
    private static final String TABLE_IMAGES_NAME = "tb4_images";
    // private static final String TABLE_IMAGES_INDEX_NAME = "tb4_url_images_index";
    private static final String TABLE_IMAGES_INDEX2_NAME = "tb4_images_index";
    private static final String TABLE_QUERIES_NAME = "tb5_queries";
    private static final String TABLE_USER_URLS_NAME = "tb6_user_urls";
    private static final String TABLE_IMAGE_WORDS_NAME = "tb7_image_words";
    private static final String TABLE_IMAGE_WORDS_INDEX_NAME = "tb7_image_words_url_index";
    private static final String TABLE_IMAGE_WORDS_INDEX2_NAME = "tb7_image_stem_url_index";
    private static final String TABLE_TRENDS_NAME = "tb8_trends";

    // SQL statement used to create the database
    private static final String TABLE1_CREATE = String.format(
            "CREATE TABLE if not exists %s( %s INTEGER PRIMARY KEY AUTO_INCREMENT, %s varchar(256),"
                    + " %s MEDIUMTEXT, %s varchar(512), %s DATETIME, %s BOOLEAN DEFAULT false, %s double DEFAULT 0,"
                    + " %s double DEFAULT 0, %s double DEFAULT 0)",
            TABLE_URLS_NAME, COL_ID, COL_URL, COL_CONTENT, COL_TITLE, COL_CRAWLED_AT, COL_INDEXED, COL_PAGE_RANK,
            COL_DATE_SCORE, COL_GEO_SCORE);

    private static final String TABLE1_INDEX_CREATE = String.format("CREATE UNIQUE INDEX if not exists %s ON %s(%s)",
            TABLE_URLS_INDEX_NAME, TABLE_URLS_NAME, COL_URL);

    private static final String TABLE2_CREATE = String.format(
            "CREATE TABLE if not exists %s(%s INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + " %s varchar(100), %s varchar(100), %s varchar(256), %s DOUBLE, FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE)",
            TABLE_WORDS_NAME, COL_ID, COL_WORD, COL_STEM, COL_URL, COL_SCORE, COL_URL, TABLE_URLS_NAME, COL_URL);

    private static final String TABLE2_INDEX_CREATE = String.format(
            "CREATE INDEX if not exists %s ON %s(%s, %s)", TABLE_WORDS_INDEX_NAME, TABLE_WORDS_NAME, COL_WORD,
            COL_URL);

    private static final String TABLE2_INDEX2_CREATE = String.format(
            "CREATE INDEX if not exists %s ON %s(%s, %s)", TABLE_WORDS_INDEX2_NAME, TABLE_WORDS_NAME, COL_STEM,
            COL_URL);

    public static final String TABLE3_LINKS_CREATE = String.format(
            "CREATE TABLE IF NOT EXISTS %s( %s INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + " %s varchar(256), %s varchar(256), FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE,"
                    + " FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE)",
            TABLE_LINKS_NAME, COL_ID, COL_SRC_URL, COL_DST_URL, COL_SRC_URL, TABLE_URLS_NAME, COL_URL, COL_DST_URL,
            TABLE_URLS_NAME, COL_URL);

    private static final String TABLE3_INDEX_CREATE = String.format(
            "CREATE INDEX if not exists %s ON %s(%s, %s)", TABLE_LINKS_INDEX_NAME, TABLE_LINKS_NAME,
            COL_SRC_URL, COL_DST_URL);

    public static final String TABLE4_IMAGES_CREATE = String.format(
                "CREATE TABLE IF NOT EXISTS %s( %s INTEGER PRIMARY KEY AUTO_INCREMENT,"
                        + " %s varchar(256), %s varchar(512), %s TEXT, FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE)",
                TABLE_IMAGES_NAME, COL_ID, COL_URL, COL_IMAGE, COL_ALT, COL_URL, TABLE_URLS_NAME, COL_URL);
    
    // private static final String TABLE4_INDEX_CREATE = String.format(
    //             "CREATE INDEX if not exists %s ON %s(%s, %s)", TABLE_IMAGES_INDEX_NAME, TABLE_IMAGES_NAME,
    //             COL_URL, COL_IMAGE);

    private static final String TABLE4_INDEX2_CREATE = String.format("CREATE UNIQUE INDEX if not exists %s ON %s(%s)",
            TABLE_IMAGES_INDEX2_NAME, TABLE_IMAGES_NAME, COL_IMAGE);

    private static final String TABLE5_QUERIES_CREATE = String.format(
            "CREATE TABLE IF NOT EXISTS %s(%s INTEGER PRIMARY KEY AUTO_INCREMENT, %s TEXT UNIQUE)", TABLE_QUERIES_NAME, COL_ID,
            COL_QUERY);

    private static final String TABLE6_USER_URLS_CREATE = String.format(
            "CREATE TABLE IF NOT EXISTS %s(%s INTEGER PRIMARY KEY AUTO_INCREMENT, %s varchar(256) UNIQUE, %s INTEGER)",
            TABLE_USER_URLS_NAME, COL_ID, COL_URL, COL_FREQ);

    private static final String TABLE7_CREATE = String.format(
            "CREATE TABLE if not exists %s(%s INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + " %s varchar(100), %s varchar(100), %s varchar(512), FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE)",
            TABLE_IMAGE_WORDS_NAME, COL_ID, COL_WORD, COL_STEM, COL_IMAGE, COL_IMAGE, TABLE_IMAGES_NAME, COL_IMAGE);

    private static final String TABLE7_INDEX_CREATE = String.format(
            "CREATE UNIQUE INDEX if not exists %s ON %s(%s, %s)", TABLE_IMAGE_WORDS_INDEX_NAME, TABLE_IMAGE_WORDS_NAME, COL_WORD,
            COL_IMAGE);

    private static final String TABLE7_INDEX2_CREATE = String.format(
            "CREATE INDEX if not exists %s ON %s(%s, %s)", TABLE_IMAGE_WORDS_INDEX2_NAME, TABLE_IMAGE_WORDS_NAME, COL_STEM,
            COL_IMAGE);

    private static final String TABLE8_CREATE = String.format(
                "CREATE TABLE IF NOT EXISTS %s(%s varchar(256) PRIMARY KEY, %s varchar(256), %s INTEGER)",
                TABLE_TRENDS_NAME, COL_PERSON, COL_REGION, COL_COUNT);

    private static final String DATABASE_CREATE = String.format("CREATE DATABASE IF NOT EXISTS %s", DATABASE_NAME);

    public IndexerDbAdapter() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement stmt = conn.createStatement()) {
            stmt.addBatch(TABLE1_CREATE);
            stmt.addBatch(TABLE1_INDEX_CREATE);
            stmt.addBatch(TABLE2_CREATE);
            stmt.addBatch(TABLE2_INDEX_CREATE);
            stmt.addBatch(TABLE2_INDEX2_CREATE);
            stmt.addBatch(TABLE3_LINKS_CREATE);
            stmt.addBatch(TABLE3_INDEX_CREATE);
            stmt.addBatch(TABLE4_IMAGES_CREATE);
            // stmt.addBatch(TABLE4_INDEX_CREATE);
            stmt.addBatch(TABLE4_INDEX2_CREATE);
            stmt.addBatch(TABLE5_QUERIES_CREATE);
            stmt.addBatch(TABLE6_USER_URLS_CREATE);
            stmt.addBatch(TABLE7_CREATE);
            stmt.addBatch(TABLE7_INDEX_CREATE);
            stmt.addBatch(TABLE7_INDEX2_CREATE);
            stmt.addBatch(TABLE8_CREATE);
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void open() {
        try {

            conn = DriverManager.getConnection(CONNECTION_STRING, USERNAME, PASSWORD);

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(DATABASE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            conn.close();

            Properties info = new Properties();
            info.setProperty("user", USERNAME);
            info.setProperty("password", PASSWORD);
            info.setProperty("cachePrepStmts", "true");
            info.setProperty("prepStmtCacheSize", "250");
            info.setProperty("prepStmtCacheSqlLimit", "2048");
            info.setProperty("useServerPrepStmts", "true");
            info.setProperty("useLocalSessionState", "true");
            info.setProperty("rewriteBatchedStatements", "true");
            info.setProperty("cacheResultSetMetadata", "true");
            info.setProperty("cacheServerConfiguration", "true");
            info.setProperty("elideSetAutoCommits", "true");
            info.setProperty("maintainTimeStats", "false");
            conn = DriverManager.getConnection(CONNECTION_STRING + DATABASE_NAME, info);
            createTables();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getDocumentsNum() {

        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + TABLE_URLS_NAME)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * add a trend to the database
     * 
     * @param word the search word to be added to database
     * @param region the region of the user
     * @return whether the person is added successfully or not
     */
    public boolean addTrend(String person, String region) {
        String sql = String.format("INSERT INTO %s(%s, %s, %s) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE %s = %s + 1",
                 TABLE_TRENDS_NAME, COL_PERSON, COL_REGION, COL_COUNT, COL_COUNT, COL_COUNT);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, person);
            ps.setString(2, region);
            ps.setInt(3, 1);
            ps.executeUpdate();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

     /**
     * @return most searched persons and their counts
     */
    public ArrayList<Pair<String,Integer>> fetchTrends() {
        String sql = String.format("SELECT %s, %s FROM %s LIMIT 10", COL_PERSON, COL_COUNT, TABLE_TRENDS_NAME);
        ArrayList<Pair<String,Integer>> ret = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Pair<String,Integer> elem = new Pair<String,Integer>(rs.getString(1), rs.getInt(2));
                    ret.add(elem);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    /**
     * add url to the database
     * 
     * @param url the url to be added to database
     * @return whether the url is added successfully
     */
    public boolean addURL(String url) {
        String sql = String.format("INSERT INTO %s(%s) VALUES(?)", TABLE_URLS_NAME, COL_URL);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, url);
            ps.executeUpdate();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }
    
    /**
     * change the crawling date of a url and set indexed attribute to false
     * 
     * @param url the url to change its crawled_date
     */
    public void crawlURL(String url) {
        String sql = String.format("UPDATE %s set %s = now(), %s = false WHERE %s = ?", TABLE_URLS_NAME, COL_CRAWLED_AT,
                COL_INDEXED, COL_URL);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, url);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return all URLs to be crawled
     */
    public ArrayList<String> getUnCrawledURLs() {
        String sql = String.format("SELECT %s FROM %s WHERE %s IS NULL", COL_URL, TABLE_URLS_NAME, COL_CRAWLED_AT);
        ArrayList<String> result = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @return all crawled urls
     */
    public ArrayList<String> getCrawledURLs() {
        String sql = String.format("SELECT %s FROM %s WHERE %s IS NOT NULL", COL_URL, TABLE_URLS_NAME, COL_CRAWLED_AT);
        ArrayList<String> result = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @return URLs sorted by most recent urls
     */
    public ArrayList<String> getURLsToBeRecrawled() {
        String sql = String.format("SELECT %s FROM %s ORDER BY %s DESC", COL_URL, TABLE_URLS_NAME, COL_DATE_SCORE);
        ArrayList<String> result = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * update a url
     * 
     * @param url        the url to be updated
     * @param content    the full plain text of the url without tags
     * @param date_score the date score of the url (recent pages are favored to old
     *                   pages)
     * @param geo_score  the geographic location score of the url
     */
    public void updateURL(String url, String content, String title, double date_score, double geo_score) {
        String sql = String.format("UPDATE %s set %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ?", TABLE_URLS_NAME, COL_CONTENT,
                COL_TITLE, COL_DATE_SCORE, COL_GEO_SCORE, COL_URL);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, content);
            ps.setString(2, title);
            ps.setDouble(3, date_score);
            ps.setDouble(4, geo_score);
            ps.setString(5, url);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update a group of urls using batch
     * @param pages array of pages
     */
    public void updateAllURLS(ArrayList<Page>pages){
        String sql = String.format("UPDATE %s set %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ?", TABLE_URLS_NAME, COL_CONTENT,
                COL_TITLE, COL_DATE_SCORE, COL_GEO_SCORE, COL_URL);
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            for(int i=0;i<pages.size();i++){
                ps.setString(1, pages.get(i).getContent());
                ps.setString(2, pages.get(i).getTitle());
                ps.setDouble(3, pages.get(i).getDateScore());
                ps.setDouble(4, pages.get(i).getGeoScore());
                ps.setString(5, pages.get(i).getUrl());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }  
    }

    /**
     * update a url
     * 
     * @param url      the url to be updated
     * @param pageRank the rank of the url (used in page ranking)
     */
    public void updateURL(String url, double page_rank) {
        String sql = String.format("UPDATE %s set %s = ? WHERE %s = ?", TABLE_URLS_NAME, COL_PAGE_RANK, COL_URL);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, page_rank);
            ps.setString(2, url);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * update pages ranks
     * 
     * @param ranks hashmap of each url and its rank
     */
    public void updatePagesRanks(HashMap<String, Double> ranks) {
        String sql = String.format("UPDATE %s set %s = ? WHERE %s = ?", TABLE_URLS_NAME, COL_PAGE_RANK, COL_URL);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Entry<String, Double> page: ranks.entrySet()) {
                ps.setDouble(1, page.getValue());
                ps.setString(2, page.getKey());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * set indexed attribute of url
     * 
     * @param url     the url to set its indexed attribute
     * @param indexed whether the url is indexed or not
     */
    public void setIndexedURL(String url, boolean indexed) {
        String sql = String.format("UPDATE %s set %s = ? WHERE %s = ?", TABLE_URLS_NAME, COL_INDEXED, COL_URL);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, indexed);
            ps.setString(2, url);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return first un-indexed URL or null if not found
     */
    public String getUnindexedURL() {
        String sql = String.format("SELECT %s FROM %s WHERE %s IS false LIMIT 1", COL_URL, TABLE_URLS_NAME,
                COL_INDEXED);
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next())
                return rs.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void resetPagesRank() {
        String sql = String.format("UPDATE %s SET %s = %s", TABLE_URLS_NAME, COL_PAGE_RANK, 1.0 / getDocumentsNum());
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * add all words of a certian url
     * 
     * @param wordsScores hashmap of words with their scores
     * @param url         the url to add its words
     */
    public void addWords(HashMap<String, Double> wordsScores, String url) {

        String sql = String.format(
                "INSERT INTO %s(%s, %s, %s, %s) VALUES" + makeParentheses(wordsScores.size(), 4)
                        + "ON DUPLICATE KEY UPDATE %s = VALUES(%s)",
                TABLE_WORDS_NAME, COL_WORD, COL_STEM, COL_URL, COL_SCORE, COL_SCORE, COL_SCORE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Entry<String, Double> entry : wordsScores.entrySet()) {

                ps.setString(i, entry.getKey());
                ps.setString(i + 1, WordsExtractionProcess.stem(entry.getKey()));
                ps.setString(i + 2, url);
                ps.setDouble(i + 3, entry.getValue());
                i += 4;
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * add a word of a url
     * 
     * @param word  the word to add
     * @param url   the url to add its word
     * @param score score is sum of (term frequency of certian tag)*(tag score) of
     *              different htm tags of a word
     */
    public void addWord(String word, String url, double score) {
        String sql = String.format("INSERT INTO %s(%s, %s, %s, %s) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE %s = VALUES(%s)",
                TABLE_WORDS_NAME, COL_WORD, COL_STEM, COL_URL, COL_SCORE, COL_SCORE, COL_SCORE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, word);
            ps.setString(2, WordsExtractionProcess.stem(word));
            ps.setString(3, url);
            ps.setDouble(4, score);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // return true when the link is added successfully, false otherwise
    public boolean addLink(String srcURL, String dstURL) {
        String sql = String.format("INSERT INTO %s(%s, %s) VALUES(?, ?)", TABLE_LINKS_NAME, COL_SRC_URL, COL_DST_URL);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, srcURL);
            ps.setString(2, dstURL);
            ps.executeUpdate();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    /**
     * add an image of a url
     * 
     * @param image  the image to be added
     * @param url   the url to add its image
     * @return true if added sucessfully, false otherwise
     */
    public boolean addImage(String url, String image, String alt) {
        String sql = String.format("INSERT INTO %s(%s, %s, %s) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE %s = VALUES(%s)", TABLE_IMAGES_NAME, COL_URL, COL_IMAGE, COL_ALT);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, url);
            ps.setString(2, image);
            ps.setString(3, alt);
            ps.executeUpdate();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    /**
     * add all images of a url
     * @param url the url to add its imags
     * @param images list of images to be added
     */
    public void addImages(String url, ArrayList<Image> images) {
        
        String sql = String.format("INSERT INTO %s(%s, %s, %s) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE %s = VALUES(%s)",
                TABLE_IMAGES_NAME, COL_URL, COL_IMAGE, COL_ALT, COL_ALT, COL_ALT);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Image image : images) {
                ps.setString(1, url);
                ps.setString(2, image.getSrc());
                ps.setString(3, image.getAlt());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * add all words of a certian image
     * 
     * @param words       array of words
     * @param src         the image src to add its words
     */
    public void addImageWords(String src, ArrayList<String> words){
        
        String sql = String.format(
                "INSERT INTO %s(%s, %s, %s) VALUES(?, ?, ?)",
                TABLE_IMAGE_WORDS_NAME, COL_WORD, COL_STEM, COL_IMAGE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String word : words) {
                ps.setString(1, word);
                ps.setString(2, WordsExtractionProcess.stem(word));
                ps.setString(3, src);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // utility function to create placeholders (?) given a length
    private String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    /**
     * utility function to create parentheses of place holders of size n*m
     * @param n number of parantheses
     * @param m number of placeholders inside each parentheses
     * @return
     */
    private String makeParentheses(int n, int m) {
        if (n < 1) {
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder string = new StringBuilder(10 * n);
            string.append("(" + makePlaceholders(m) + ")");
            for (int i = 1; i < n; i++) {
                string.append(",(" + makePlaceholders(m) + ")");
            }
            return string.toString();
        }
    }

    /**
     * search database for the given words
     * @param words the words to search for
     * @param limit limit number of urls to be returned (recommended 10)
     * @param page  the page number of the result
     * @return list of hashmaps that contain url, content and title
     */
    public ArrayList<HashMap<String, String>> queryWords(String[] words, int limit, int page) {
        String sql = String.format("SELECT %s, %s, %s FROM %s"
                + " JOIN (SELECT %s, log((select count(*) from %s)*1.0/count(%s)) as idf FROM %s GROUP BY %s)"
                + " as temp USING (%s) JOIN %s USING (%s) LEFT JOIN (SELECT %s, count(*) as matching_words from %s"
                + " where %s in (" + makePlaceholders(words.length) + ") GROUP BY %s) as temp2 USING (%s)"
                + " WHERE %s in (" + makePlaceholders(words.length) + ") and %s < 1.7 GROUP by %s"
                + " ORDER BY (4*sum(%s*idf) +  COALESCE(matching_words, 0) + %s + 0.5*(%s + %s)) DESC LIMIT ?, ?",
                COL_URL, COL_CONTENT, COL_TITLE, TABLE_WORDS_NAME, COL_STEM, TABLE_URLS_NAME, COL_URL, TABLE_WORDS_NAME,
                COL_STEM, COL_STEM, TABLE_URLS_NAME, COL_URL, COL_URL, TABLE_WORDS_NAME, COL_WORD, COL_URL, COL_URL,
                COL_STEM, COL_SCORE, COL_URL, COL_SCORE, COL_PAGE_RANK, COL_DATE_SCORE, COL_GEO_SCORE);

        ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < words.length; i++) {
                ps.setString(i + 1, words[i]);
            }
            for (int i = 0; i < words.length; i++) {
                ps.setString(words.length + i + 1, WordsExtractionProcess.stem(words[i]));
            }
            ps.setInt(2 * words.length + 1, (page - 1) * limit);
            ps.setInt(2 * words.length + 2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    HashMap<String, String> elem = new HashMap<String, String>();
                    elem.put("url", rs.getString(1));
                    elem.put("content", rs.getString(2));
                    elem.put("title", rs.getString(3));
                    ret.add(elem);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * search for a phrase in database
     * @param phrase the phrase to search for
     * @param words splitted phrase as list of words
     * @param limit limit number of urls to be returned (recommended 10)
     * @param page  the page number of the result
     * @return list of hashmaps that contain url, content and title
     */
    public ArrayList<HashMap<String, String>> queryPhrase(String phrase, String[] words, int limit, int page) {
        String sql = String.format("SELECT %s, %s, %s FROM %s"
                + " JOIN (SELECT %s, log((select count(*) from %s)*1.0/count(%s)) as idf FROM %s GROUP BY %s)"
                + " as temp USING (%s) JOIN %s USING (%s) WHERE %s in (" + makePlaceholders(words.length)
                + ") and %s < 1.7 and %s like ? GROUP by %s ORDER BY (3*sum(%s*idf) + %s + 0.5*(%s + %s)) DESC LIMIT ?, ?",
                COL_URL, COL_CONTENT, COL_TITLE, TABLE_WORDS_NAME, COL_STEM, TABLE_URLS_NAME, COL_URL, TABLE_WORDS_NAME,
                COL_STEM, COL_STEM, TABLE_URLS_NAME, COL_URL, COL_STEM, COL_SCORE, COL_CONTENT, COL_URL, COL_SCORE,
                COL_PAGE_RANK, COL_DATE_SCORE, COL_GEO_SCORE);

        ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < words.length; i++) {
                ps.setString(i + 1, WordsExtractionProcess.stem(words[i]));
            }

            // escape wildcard characters in phrase query
            phrase = phrase.replace("%", "\\%").replace("_", "\\_");
            ps.setString(words.length + 1, "%" + phrase + "%");
            ps.setInt(words.length + 2, (page - 1) * limit);
            ps.setInt(words.length + 3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    HashMap<String, String> elem = new HashMap<String, String>();
                    elem.put("url", rs.getString(1));
                    elem.put("content", rs.getString(2));
                    elem.put("title", rs.getString(3));
                    ret.add(elem);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * get images from database by phrase
     * 
     * @param phrase the phrase to search for
     * @param limit limit number of urls to be returned (recommended 10)
     * @param page  the page number of the result
     * @return list of hashmaps that contain url and image
     * 
     */
    public ArrayList<HashMap<String, String>> queryImage(String phrase, int limit, int page) {
        String sql = String.format("SELECT %s, %s, %s FROM %s JOIN %s USING (%s)"
                + " where %s like ? ORDER BY ( %s + 0.5*(%s + %s)) DESC LIMIT ?, ?",
                COL_URL, COL_IMAGE, COL_ALT, TABLE_IMAGES_NAME, TABLE_URLS_NAME, COL_URL, COL_ALT, COL_PAGE_RANK, COL_DATE_SCORE,
                COL_GEO_SCORE);

        ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            // escape wildcard characters in phrase query
            phrase = phrase.replace("%", "\\%").replace("_", "\\_");
            ps.setString(1, "%" + phrase + "%");
            ps.setInt(2, (page - 1) * limit);
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    HashMap<String, String> elem = new HashMap<String, String>();
                    elem.put("url", rs.getString(1));
                    elem.put("image", rs.getString(2));
                    elem.put("alt", rs.getString(3));
                    ret.add(elem);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * get images from database by words
     * 
     * @param words  the list of words
     * @param limit   the number of results in a page
     * @param page  page number
     * @return list of hashmaps of url and image
     */
    public ArrayList<HashMap<String, String>> queryImage(String[] words, int limit, int page) {
        String sql = String.format(
                "SELECT %s, %s, %s FROM %s JOIN %s USING(%s) JOIN %s USING (%s)"
                        + " LEFT JOIN (SELECT %s, COUNT(*) AS matching_words FROM %s WHERE %s in ("
                        + makePlaceholders(words.length) + ")" + "GROUP BY %s) as temp USING(%s) WHERE %s in("
                        + makePlaceholders(words.length) + ") GROUP BY %s"
                        + " ORDER BY (COALESCE(matching_words, 0) + %s + 0.5*(%s + %s)) DESC LIMIT ?, ?",
                COL_URL, COL_IMAGE, COL_ALT, TABLE_IMAGES_NAME, TABLE_IMAGE_WORDS_NAME, COL_IMAGE, TABLE_URLS_NAME, COL_URL,
                COL_IMAGE, TABLE_IMAGE_WORDS_NAME, COL_WORD, COL_IMAGE, COL_IMAGE, COL_STEM, COL_IMAGE, COL_PAGE_RANK,
                COL_DATE_SCORE, COL_GEO_SCORE);

        ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < words.length; i++) {
                ps.setString(i + 1, words[i]);
            }
            for (int i = 0; i < words.length; i++) {
                ps.setString(words.length + i+1, WordsExtractionProcess.stem(words[i]));    
            }

            ps.setInt(2 * words.length + 1, (page - 1) * limit);
            ps.setInt(2 * words.length + 2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    HashMap<String, String> elem = new HashMap<String, String>();
                    elem.put("url", rs.getString(1));
                    elem.put("image", rs.getString(2));
                    elem.put("alt", rs.getString(3));
                    ret.add(elem);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * add user query in database
     * @param query the query of the user
     */
    public boolean addQuery(String query) {
        String sql = String.format("INSERT INTO %s(%s) VALUES(?)", TABLE_QUERIES_NAME, COL_QUERY);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, query);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * fetch all queries inserted by all users
     * 
     * @return array of strings of queries of users
     */
    public ArrayList<String> fetchAllQueries() {
        String sql = String.format("SELECT %s FROM %s", COL_QUERY, TABLE_QUERIES_NAME);
        ArrayList<String> ret = new ArrayList<String>();
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    ret.add(rs.getString(1));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * add urls that the user clicks on, this function is used in personalized search
     * @param url the url that the user clicked on
     */
    public void addUserURL(String url) {
        String sql = String.format("INSERT INTO %s(%s, %s) VALUES(?, 1) ON DUPLICATE KEY UPDATE %s = VALUES(%s)+1",
                TABLE_USER_URLS_NAME, COL_URL, COL_FREQ, COL_FREQ, COL_FREQ);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, url);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param url the url to return its frequency
     * @return the frequency of clicks on url
     */
    public int getUserURLFreq(String url) {
        String sql = String.format("SELECT %s FROM %s WHERE %s = ?", COL_FREQ, TABLE_USER_URLS_NAME, COL_URL);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, url);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @return all links that are used in page rank
     */
    public ArrayList<Pair<String,String>> fetchAllLinks() {
        String sql = String.format("SELECT %s, %s FROM %s", COL_SRC_URL, COL_DST_URL, TABLE_LINKS_NAME);
        ArrayList<Pair<String,String>> ret = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Pair<String,String> elem = new Pair<String,String>(rs.getString(1), rs.getString(2));
                    ret.add(elem);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public int removeDuplicateImages() {
        String sql = String.format(
                "DELETE c1 FROM %s c1 INNER JOIN %s c2 WHERE c1.%s > c2.%s AND c1.%s = c2.%s AND c1.%s = c2.%s;",
                TABLE_IMAGES_NAME, TABLE_IMAGES_NAME, COL_ID, COL_ID, COL_URL, COL_URL, COL_IMAGE, COL_IMAGE);
        try (Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int removeDuplicateLinks() {
        String sql = String.format(
                "DELETE c1 FROM %s c1 INNER JOIN %s c2 WHERE c1.%s > c2.%s AND c1.%s = c2.%s AND c1.%s = c2.%s;",
                TABLE_LINKS_NAME, TABLE_LINKS_NAME, COL_ID, COL_ID, COL_SRC_URL, COL_SRC_URL, COL_DST_URL, COL_DST_URL);
        try (Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int removeDuplicateWords() {
        String sql = String.format(
                "DELETE c1 FROM %s c1 INNER JOIN %s c2 WHERE c1.%s > c2.%s AND c1.%s = c2.%s AND c1.%s = c2.%s;",
                TABLE_WORDS_NAME, TABLE_WORDS_NAME, COL_ID, COL_ID, COL_WORD, COL_WORD, COL_URL, COL_URL);
        try (Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int removeDuplicateImageWords() {
        String sql = String.format(
                "DELETE c1 FROM %s c1 INNER JOIN %s c2 WHERE c1.%s > c2.%s AND c1.%s = c2.%s AND c1.%s = c2.%s;",
                TABLE_IMAGE_WORDS_NAME, TABLE_IMAGE_WORDS_NAME, COL_ID, COL_ID, COL_WORD, COL_WORD, COL_IMAGE,
                COL_IMAGE);
        try (Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void deleteURL(String url) {
        String sql = String.format("DELETE FROM %s WHERE %s = ?", TABLE_URLS_NAME, COL_URL);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, url);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void deleteAllURLS() {
        String sql = String.format("DELETE FROM %s", TABLE_WORDS_NAME);
        try (Statement stmt = conn.createStatement()) {
            stmt.addBatch(sql);
            sql = String.format("DELETE FROM %s", TABLE_URLS_NAME);
            stmt.addBatch(sql);
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}

