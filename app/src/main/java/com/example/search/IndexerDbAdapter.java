package com.example.search;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class IndexerDbAdapter {

    // contains 2 tables, one has columns: URL, content, page_rank
    // the other has columns: word, URL, tf, word_tag

    //these are the column names
    public static final String COL_ID = "_id";
    public static final String COL_URL = "url";
    // content of the url after removing tags (used in phrase search)
    public static final String COL_CONTENT = "content";
    public static final String COL_PAGE_RANK = "page_rank";

    public static final String COL_WORD = "word";
    // normalized term frequency (word_count/number of words in the document)
    public static final String COL_TF = "tf";
    // score of different html tags of a word
    public static final String COL_WORD_TAG = "word_tag";


    public static final String COL_SRC_URL = "src_url";
    public static final String COL_DST_URL = "dst_url";

    //these are the corresponding indices
    public static final int INDEX_ID = 0;
    public static final int INDEX_URL = INDEX_ID + 1;
    public static final int INDEX_CONTENT = INDEX_ID + 2;
    public static final int INDEX_PAGE_RANK = INDEX_ID + 3;

    public static final int INDEX_WORD = INDEX_ID + 1;
    public static final int INDEX_URL_TABLE2 = INDEX_ID + 2;
    public static final int INDEX_TF = INDEX_ID + 3;
    public static final int INDEX_TF_IDF = INDEX_ID + 4;
    public static final int INDEX_WORD_TAG = INDEX_ID + 5;

    public static final int INDEX_SRC_URL = INDEX_ID + 1;
    public static final int INDEX_DST_URL = INDEX_ID + 2;

    //used for logging
    private static final String TAG = "IndexerDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private static final String DATABASE_NAME = "dba_search_indexer";
    private static final String TABLE_URLS_NAME = "tb1_urls";
    private static final String TABLE_URLS_INDEX_NAME = "tbl_urls_index";
    private static final String TABLE_WORDS_NAME = "tb2_words";
    private static final String TABLE_WORDS_INDEX_NAME = "tb2_words_url_index";
    private static final String TABLE_LINKS_NAME = "tb3_links";
    private static final int DATABASE_VERSION = 1;
    private final Context mCtx;
    //SQL statement used to create the database

    private static final String TABLE1_CREATE =
            "CREATE TABLE if not exists " + TABLE_URLS_NAME + " ( " +
                    COL_ID + " INTEGER PRIMARY KEY autoincrement, " +
                    COL_URL + " varchar(512), " +
                    COL_CONTENT + " TEXT, " +
                    COL_PAGE_RANK + " double);";

    private static final String TABLE1_INDEX_CREATE =
            "CREATE UNIQUE INDEX if not exists " + TABLE_URLS_INDEX_NAME +
                    " ON " + TABLE_URLS_NAME + "(" + COL_URL + ");";

    private static final String TABLE2_CREATE =
            "CREATE TABLE if not exists " + TABLE_WORDS_NAME + " ( " +
                    COL_ID + " INTEGER PRIMARY KEY autoincrement, " +
                    COL_WORD + " varchar(512), " +
                    COL_URL + " varchar(512),  " +
                    COL_TF + " DOUBLE, " +
                    COL_WORD_TAG + " INTEGER, " +
                    "FOREIGN KEY (" + COL_URL + ") REFERENCES " +
                    TABLE_URLS_NAME + "(" + COL_URL + ") ON DELETE CASCADE);";

    private static final String TABLE2_INDEX_CREATE =
            "CREATE UNIQUE INDEX if not exists " + TABLE_WORDS_INDEX_NAME +
                    " ON " + TABLE_WORDS_NAME + "(" + COL_WORD + ", " + COL_URL + ");";

    private static final String TABLE3_LINKS_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_LINKS_NAME + " ( " +
                    COL_ID + " INTEGER PRIMARY KEY autoincrement, " +
                    COL_SRC_URL + " varchar(512), " +
                    COL_DST_URL + " varchar(512)), " +
                    "FOREIGN KEY (" + COL_SRC_URL + ") REFERENCES " +
                    TABLE_URLS_NAME + "(" + COL_URL + ") ON DELETE CASCADE), " +
                    "FOREIGN KEY (" + COL_DST_URL + ") REFERENCES " +
                    TABLE_URLS_NAME + "(" + COL_URL + ") ON DELETE CASCADE);";

    private static final String DATABASE_CREATE =
            TABLE1_CREATE + TABLE1_INDEX_CREATE + TABLE2_CREATE + TABLE2_INDEX_CREATE +
            TABLE3_LINKS_CREATE;

    private static final String TABLE1_DROP =
            "DROP TABLE IF EXISTS " + TABLE_URLS_NAME;
    private static final String TABLE1_INDEX_DROP =
            "DROP TABLE IF EXISTS " + TABLE_URLS_INDEX_NAME;
    private static final String TABLE2_DROP =
            "DROP TABLE IF EXISTS " + TABLE_WORDS_NAME;
    private static final String TABLE2_INDEX_DROP =
            "DROP TABLE IF EXISTS " + TABLE_WORDS_INDEX_NAME;
    private static final String TABLE3_DROP =
            "DROP TABLE IF EXISTS " + TABLE_LINKS_NAME;

    private static final String DATABASE_DROP =
            TABLE1_DROP + TABLE1_INDEX_DROP + TABLE2_DROP + TABLE2_INDEX_DROP + TABLE3_DROP;

    public IndexerDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    //open
    public void open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
    }
    //close
    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    public int getDocumentsNum() {
        Cursor cursor = mDb.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_URLS_NAME,
                new String[]{}
        );
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    // content is the plain text of the url without tags
    public void addURL(String url, String content) {
        ContentValues values = new ContentValues();
        values.put(COL_URL, url);
        values.put(COL_CONTENT, content);
        values.put(COL_PAGE_RANK, 1.0/getDocumentsNum());
        mDb.replace(TABLE_URLS_NAME, null, values);
    }

    public void resetPagesRank() {
        ContentValues values = new ContentValues();
        values.put(COL_PAGE_RANK, 1.0/getDocumentsNum());
        mDb.update(TABLE_URLS_NAME, values, null, null);
    }

    // wordTag is sum of scores of different html tags of a word
    public void addWord(String word, String url, double tf, int wordTag) {
        ContentValues values = new ContentValues();
        values.put(COL_WORD, word);
        values.put(COL_URL, url);
        values.put(COL_TF, tf);
        values.put(COL_WORD_TAG, wordTag);
        mDb.replace(TABLE_WORDS_NAME, null, values);
    }

    public void addLink(String srcURL, String dstURL) {
        ContentValues values = new ContentValues();
        values.put(COL_SRC_URL, srcURL);
        values.put(COL_DST_URL, dstURL);
        mDb.replace(TABLE_LINKS_NAME, null, values);
    }

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

    // `page` is the page number in search result,
    // each page of search results contains `limit` urls
    public ArrayList<String> queryWords(String[] words, int limit, int page) {
        Cursor cursor =  mDb.rawQuery(
                "SELECT url, sum(tf) " +
                "FROM " + TABLE_WORDS_NAME +
                " WHERE word IN ("+makePlaceholders(words.length)+") AND tf < 0.5" +
                " GROUP BY url " +
                " ORDER BY tf DESC, word_tag DESC " +
                " LIMIT " + (page-1)*limit + ", " + limit,
                words
        );
        ArrayList<String> ret = new ArrayList<String>();
        for (cursor.moveToFirst(); !cursor.isAfterLast() ; cursor.moveToNext()) {
            ret.add(cursor.getString(0));
        }
        return ret;
    }

    public ArrayList<String> queryPhrase(String phrase, int limit, int page) {
        Cursor cursor = mDb.query(
                TABLE_URLS_NAME,
                new String[] {COL_URL},
                COL_CONTENT + " LIKE %?%",
                new String[] {phrase},
                null,
                null,
                null,
                (page-1)*limit + ", " + limit
        );
        ArrayList<String> ret = new ArrayList<String>();
        for (cursor.moveToFirst(); !cursor.isAfterLast() ; cursor.moveToNext()) {
            ret.add(cursor.getString(0));
        }
        return ret;
    }

    public void deleteURL(String url) {
        mDb.delete(
                TABLE_URLS_NAME,
                COL_URL +"=?",
                new String[] {url}
        );
    }

    public void deleteAllURLS() {
        mDb.delete(TABLE_WORDS_NAME, null, null);
        mDb.delete(TABLE_URLS_NAME, null, null);
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL(DATABASE_DROP);
            onCreate(db);
        }
    }
}
