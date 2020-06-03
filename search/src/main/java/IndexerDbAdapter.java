
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

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
    public static final String COL_SCORE = "score";

    public static final String COL_SRC_URL = "src_url";
    public static final String COL_DST_URL = "dst_url";

    public static final String COL_IMAGE = "image";

    private Connection conn;
    private static final String DATABASE_NAME = "dba_search_indexer";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/";
    private static final String TABLE_URLS_NAME = "tb1_urls";
    private static final String TABLE_URLS_INDEX_NAME = "tbl_urls_index";
    private static final String TABLE_WORDS_NAME = "tb2_words";
    private static final String TABLE_WORDS_INDEX_NAME = "tb2_words_url_index";
    private static final String TABLE_LINKS_NAME = "tb3_links";
    private static final String TABLE_LINKS_INDEX_NAME = "tb3_links_index";
    private static final String TABLE_IMAGES_NAME = "tb4_images";
    private static final String TABLE_IMAGES_INDEX_NAME = "tb4_images_index";

    // SQL statement used to create the database
    private static final String TABLE1_CREATE = String.format(
            "CREATE TABLE if not exists %s( %s INTEGER PRIMARY KEY AUTO_INCREMENT, %s varchar(256),"
                    + " %s TEXT, %s varchar(512), %s DATETIME, %s BOOLEAN DEFAULT false, %s double DEFAULT 0,"
                    + " %s double DEFAULT 0, %s double DEFAULT 0)",
            TABLE_URLS_NAME, COL_ID, COL_URL, COL_CONTENT, COL_TITLE, COL_CRAWLED_AT, COL_INDEXED, COL_PAGE_RANK,
            COL_DATE_SCORE, COL_GEO_SCORE);

    private static final String TABLE1_INDEX_CREATE = String.format("CREATE UNIQUE INDEX if not exists %s ON %s(%s)",
            TABLE_URLS_INDEX_NAME, TABLE_URLS_NAME, COL_URL);

    private static final String TABLE2_CREATE = String.format(
            "CREATE TABLE if not exists %s(%s INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + " %s varchar(256), %s varchar(256), %s DOUBLE, FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE)",
            TABLE_WORDS_NAME, COL_ID, COL_WORD, COL_URL, COL_SCORE, COL_URL, TABLE_URLS_NAME, COL_URL);

    private static final String TABLE2_INDEX_CREATE = String.format(
            "CREATE UNIQUE INDEX if not exists %s ON %s(%s, %s)", TABLE_WORDS_INDEX_NAME, TABLE_WORDS_NAME, COL_WORD,
            COL_URL);

    public static final String TABLE3_LINKS_CREATE = String.format(
            "CREATE TABLE IF NOT EXISTS %s( %s INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + " %s varchar(256), %s varchar(256), FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE,"
                    + " FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE)",
            TABLE_LINKS_NAME, COL_ID, COL_SRC_URL, COL_DST_URL, COL_SRC_URL, TABLE_URLS_NAME, COL_URL, COL_DST_URL,
            TABLE_URLS_NAME, COL_URL);

    private static final String TABLE3_INDEX_CREATE = String.format(
            "CREATE UNIQUE INDEX if not exists %s ON %s(%s, %s)", TABLE_LINKS_INDEX_NAME, TABLE_LINKS_NAME,
            COL_SRC_URL, COL_DST_URL);

    public static final String TABLE4_IMAGES_CREATE = String.format(
                "CREATE TABLE IF NOT EXISTS %s( %s INTEGER PRIMARY KEY AUTO_INCREMENT,"
                        + " %s varchar(256), %s varchar(256), FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE)",
                TABLE_IMAGES_NAME, COL_ID, COL_URL, COL_IMAGE, COL_URL, TABLE_URLS_NAME, COL_URL);
    
    private static final String TABLE4_INDEX_CREATE = String.format(
                "CREATE UNIQUE INDEX if not exists %s ON %s(%s, %s)", TABLE_IMAGES_INDEX_NAME, TABLE_IMAGES_NAME,
                COL_URL, COL_IMAGE);

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
            stmt.addBatch(TABLE3_LINKS_CREATE);
            stmt.addBatch(TABLE3_INDEX_CREATE);
            stmt.addBatch(TABLE4_IMAGES_CREATE);
            stmt.addBatch(TABLE4_INDEX_CREATE);
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

            conn = DriverManager.getConnection(CONNECTION_STRING + DATABASE_NAME + "?rewriteBatchedStatements=true", USERNAME, PASSWORD);
            createTables();
            conn.setAutoCommit(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (conn != null) {
            try {
                conn.commit();
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
     * update a urls
     * 
     * @param url        the url to be updated
     * @param content    the full plain text of the url without tags
     * @param date_score the date score of the url (recent pages are favored to old
     *                   pages)
     * @param geo_score  the geographic location score of the url
     * @throws SQLException
     */
    public void updateAllURLS(ArrayList<Page>pages) throws SQLException {
        String sql = String.format("UPDATE %s set %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ?", TABLE_URLS_NAME, COL_CONTENT,
                COL_TITLE, COL_DATE_SCORE, COL_GEO_SCORE, COL_URL);
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        for(int i=0;i<pages.size();i++){
            preparedStatement.setString(1, pages.get(i).content);
            preparedStatement.setString(2, pages.get(i).title);
            preparedStatement.setDouble(3, pages.get(i).dateScore);
            preparedStatement.setDouble(4, pages.get(i).geographicScroe);
            preparedStatement.setString(5, pages.get(i).url);
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
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
                "INSERT INTO %s(%s, %s, %s) VALUES" + makeParentheses(wordsScores.size(), 3)
                        + "ON DUPLICATE KEY UPDATE %s = VALUES(%s)",
                TABLE_WORDS_NAME, COL_WORD, COL_URL, COL_SCORE, COL_SCORE, COL_SCORE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Entry<String, Double> entry : wordsScores.entrySet()) {

                ps.setString(i, entry.getKey());
                ps.setString(i + 1, url);
                ps.setDouble(i + 2, entry.getValue());
                i += 3;
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // score is sum of (term frequency of certain tag)*(tag score) of different html
    // tags of a word
    /**
     * add a word of a url
     * 
     * @param word  the word to add
     * @param url   the url to add its word
     * @param score score is sum of (term frequency of certian tag)*(tag score) of
     *              different htm tags of a word
     */
    public void addWord(String word, String url, double score) {
        String sql = String.format("INSERT INTO %s(%s, %s, %s) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE %s = VALUES(%s)",
                TABLE_WORDS_NAME, COL_WORD, COL_URL, COL_SCORE, COL_SCORE, COL_SCORE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, word);
            ps.setString(2, url);
            ps.setDouble(3, score);
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
    public boolean addImage(String url, String image) {
        String sql = String.format("INSERT INTO %s(%s, %s) VALUES(?, ?)", TABLE_IMAGES_NAME, COL_URL, COL_IMAGE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, url);
            ps.setString(2, image);
            ps.executeUpdate();
        } catch (SQLException e) {
            return false;
        }
        return true;
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
    // `page` is the page number in search result,
    // each page of search results contains `limit` urls
    public ArrayList<HashMap<String, String>> queryWords(String[] words, int limit, int page) {
        String sql = String.format(
                "SELECT %s, %s, %s from( SELECT %s, %s, %s, sum(%s*idf) as words_score, %s, %s, %s FROM %s"
                        + " JOIN (SELECT %s, log((select count(*) from %s)*1.0/count(%s)) as idf FROM %s GROUP BY %s)"
                        + " as temp USING (%s) JOIN %s USING (%s) WHERE %s in (" + makePlaceholders(words.length)
                        + ") and %s < 0.6 GROUP by %s ORDER BY words_score DESC LIMIT ?, ?) as temp2"
                        + " ORDER by (words_score + %s + %s + %s) DESC",
                COL_URL, COL_CONTENT, COL_TITLE, COL_URL, COL_CONTENT, COL_TITLE, COL_SCORE, COL_PAGE_RANK, COL_DATE_SCORE, COL_GEO_SCORE,
                TABLE_WORDS_NAME, COL_WORD, TABLE_URLS_NAME, COL_URL, TABLE_WORDS_NAME, COL_WORD, COL_WORD,
                TABLE_URLS_NAME, COL_URL, COL_WORD, COL_SCORE, COL_URL, COL_PAGE_RANK, COL_DATE_SCORE, COL_GEO_SCORE);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < words.length; i++) {
                ps.setString(i + 1, words[i]);
            }
            ps.setInt(words.length + 1, (page - 1) * limit);
            ps.setInt(words.length + 2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();
                while (rs.next()) {
                    HashMap<String, String> elem = new HashMap<String, String>();
                    elem.put("url", rs.getString(1));
                    elem.put("content", rs.getString(2));
                    elem.put("title", rs.getString(3));
                    ret.add(elem);
                }
                return ret;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<HashMap<String, String>> queryPhrase(String phrase, int limit, int page) {
        String sql = String.format("SELECT %s, %s, %s FROM %s WHERE %s LIKE ? ORDER by (%s + %s + %s) DESC LIMIT ?, ?",
                COL_URL, COL_CONTENT, COL_TITLE, TABLE_URLS_NAME, COL_CONTENT, COL_PAGE_RANK, COL_DATE_SCORE, COL_GEO_SCORE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            // escape wildcard characters in phrase query
            phrase = phrase.replace("_", "%_").replace("%", "%%");
            ps.setString(1, "%%" + phrase + "%%");
            ps.setInt(2, (page - 1) * limit);
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();
                while (rs.next()) {
                    HashMap<String, String> elem = new HashMap<String, String>();
                    elem.put("url", rs.getString(1));
                    elem.put("content", rs.getString(2));
                    elem.put("title", rs.getString(3));
                    ret.add(elem);
                }
                return ret;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get images from database by phrase
     * 
     * @param phrase  the search phrase
     * @param limit   the number of results in a page
     * @param page  page number
     * 
     */
    public ArrayList<HashMap<String, String>> queryImage(String phrase, int limit, int page) {
        String sql = String.format(
            "SELECT %s.%s, %s.%s FROM %s INNER JOIN %s ON %s.%s = %s.%s WHERE %s LIKE ? ORDER by (%s + %s + %s) "
                    + " DESC LIMIT ?, ? ",
            TABLE_URLS_NAME, COL_URL, TABLE_IMAGES_NAME, COL_IMAGE, TABLE_URLS_NAME, TABLE_IMAGES_NAME,
            TABLE_IMAGES_NAME, COL_URL, TABLE_URLS_NAME, COL_URL, COL_CONTENT, COL_PAGE_RANK, COL_DATE_SCORE, COL_GEO_SCORE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            // escape wildcard characters in phrase query
            phrase = phrase.replace("_", "%_").replace("%", "%%");
            ps.setString(1, "%%" + phrase + "%%");
            ps.setInt(2, (page - 1) * limit);
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();
                while (rs.next()) {
                    HashMap<String, String> elem = new HashMap<String, String>();
                    elem.put("url", rs.getString(1));
                    elem.put("image", rs.getString(2));
                    ret.add(elem);
                }
                return ret;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * get images from database by words
     * 
     * @param words  the list of words
     * @param limit   the number of results in a page
     * @param page  page number
     * 
     */
    public ArrayList<HashMap<String, String>> queryImage(String[] words, int limit, int page) {
        String sql = String.format(
                "SELECT %s, %s from( SELECT %s, %s, %s, sum(%s*idf) as words_score, %s, %s, %s FROM %s"
                        + " JOIN (SELECT %s, log((select count(*) from %s)*1.0/count(%s)) as idf FROM %s GROUP BY %s)"
                        + " as temp USING (%s) JOIN %s USING (%s) WHERE %s in (" + makePlaceholders(words.length)
                        + ") and %s < 0.6 GROUP by %s ORDER BY words_score DESC LIMIT ?, ?) as temp2"
                        + " INNER JOIN %s USING (%s) ORDER by (words_score + %s + %s + %s) DESC",
                COL_URL, COL_IMAGE, COL_URL, COL_CONTENT, COL_TITLE, COL_SCORE, COL_PAGE_RANK, COL_DATE_SCORE, 
                COL_GEO_SCORE, TABLE_WORDS_NAME, COL_WORD, TABLE_URLS_NAME, COL_URL, TABLE_WORDS_NAME, COL_WORD, 
                COL_WORD, TABLE_URLS_NAME, COL_URL, COL_WORD, COL_SCORE, COL_URL, TABLE_IMAGES_NAME, COL_URL, 
                COL_PAGE_RANK, COL_DATE_SCORE, COL_GEO_SCORE);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < words.length; i++) {
                ps.setString(i + 1, words[i]);
            }
            ps.setInt(words.length + 1, (page - 1) * limit);
            ps.setInt(words.length + 2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();
                while (rs.next()) {
                    HashMap<String, String> elem = new HashMap<String, String>();
                    elem.put("url", rs.getString(1));
                    elem.put("image", rs.getString(2));
                    ret.add(elem);
                }
                return ret;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return all links that are used in page rank
     */
    public ArrayList<Pair> fetchAllLinks() {
        String sql = String.format("SELECT %s, %s FROM %s", COL_SRC_URL, COL_DST_URL, TABLE_LINKS_NAME);
        try (Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(sql)) {
                ArrayList<Pair> ret = new ArrayList<Pair>();
                while (rs.next()) {
                    Pair elem = new Pair(rs.getString(1), rs.getString(2));
                    ret.add(elem);
                }
                return ret;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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

