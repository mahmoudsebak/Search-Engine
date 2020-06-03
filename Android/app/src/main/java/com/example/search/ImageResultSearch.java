package com.example.search;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImageResultSearch extends AppCompatActivity {
    public static boolean endOfResult=false;
    public static String url,title;
    private static final int REQUEST_CODE = 1234;
    boolean loadingMore = false;
    Long startIndex = 0L;
    Long offset = 10L;
    View footerView;
    ImageButton voiceSearch;
    AutoCompleteTextView editText;
    int currentPage=1;

    CustomAdapterForImageSearch customAdapterForImageSearch;
    GridView imageGridView;
    ArrayList<String> sitesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_result_search);
        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_cyan));

        sitesArrayList =new ArrayList<String>();
        imageGridView =findViewById(R.id.websSteGridView);
        footerView = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.base_list_item_loading_footer, null, false);
        TextView textResult=findViewById(R.id.text_result1);
        TextView imageResult=findViewById(R.id.image_result1);
        textResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getApplicationContext(),SearchResult.class);
                i.putExtra("toText",editText.getText());
                startActivity(i);
            }
        });

        ImageButton search=findViewById(R.id.imageButton2);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkFieldsForEmptyValues(editText.getText().toString())){
                    String editTextString=editText.getText().toString();
                    editTextString=editTextString.replaceAll("\\s+","");
                    /*getResponse(
                            Request.Method.GET,
                            ULRConnection.url+"/search/query?query="+editTextString+"&page="+ 1,
                            null,
                            new VolleyCallback() {
                                @Override
                                public void onSuccessResponse(String response) throws JSONException {
                                    JSONObject obj = new JSONObject(response);
                                    try {
                                        sitesArrayList.clear();
                                        // converting response to json object
                                        // if no error in response
                                        // getting the result from the response
                                        JSONArray searchResult = obj.getJSONArray("result");
                                        for(int i=0;i<searchResult.length();i++) {
                                            String currentImage= "";
                                            JSONObject current = searchResult.getJSONObject(i);
                                            currentImage=current.getString("url");
                                            sitesArrayList.add(currentImage);
                                        }
                                        customAdapterForImageSearch.notifyDataSetChanged();
                                        endOfResult= sitesArrayList.size() == 0;
                                    } catch (JSONException e) {
                                        if(obj.isNull("result"))
                                            Toast.makeText(getApplicationContext(),"No result found",Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }
                                }
                            },editText.getText().toString(),Integer.toString(currentPage));*/
                    for(int i=0;i<10;i++)
                        sitesArrayList.add("https://upload.wikimedia.org/wikipedia/commons/thumb/6/62/Starsinthesky.jpg/220px-Starsinthesky.jpg");
                    customAdapterForImageSearch.notifyDataSetChanged();
                }
            }
        });
        voiceSearch=findViewById(R.id.search_voice_btn2);
        voiceSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });
        editText=findViewById(R.id.editText2);
        editText.setText(getIntent().getStringExtra("toImage"));
        customAdapterForImageSearch =new CustomAdapterForImageSearch(this, sitesArrayList);
        imageGridView.setAdapter(customAdapterForImageSearch);
        imageGridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
                {
                    if(!loadingMore)
                    {
                        loadingMore = true;
                        new ImageResultSearch.LoadMoreItemsTask((Activity) view.getContext()).execute();
                        imageGridView.setSelection(firstVisibleItem);
                    }

                }
            }
        });

    }
    private class LoadMoreItemsTask extends AsyncTask<Void, Void, List<String>> {

        private Activity activity;
        private View footer;

        private LoadMoreItemsTask(Activity activity) {
            this.activity = (Activity) activity;
            loadingMore = true;
            footer = ((Activity) activity).getLayoutInflater().inflate(R.layout.base_list_item_loading_footer, null);
        }

        @Override
        protected void onPreExecute() {
            if(endOfResult){
                imageGridView.setEnabled(false);
                Toast.makeText(getApplicationContext(),"No more result",Toast.LENGTH_LONG).show();
            }else {
                imageGridView.setEnabled(true);
            }
            super.onPreExecute();
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            try {
                return getNextItems(startIndex, offset);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private List<String> getNextItems(Long startIndex, Long offset) throws IOException, JSONException {
            ArrayList<String>arr=new ArrayList<>();
            /*try {
                // converting response to json object
                JSONObject obj = getJSONObjectFromURL(ULRConnection.url+"/search/query?query="+editText.getText().toString()+"&page="+ currentPage+1);
                // if no error in response
                // getting the result from the response
                JSONArray searchResult = obj.getJSONArray("result");
                for(int i=0;i<searchResult.length();i++) {
                    String currentImgURL;
                    JSONObject current = searchResult.getJSONObject(i);
                    currentImgURL=current.getString("url");
                    arr.add(currentImgURL);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(arr.size()==0)
                endOfResult=true;
            else{
                currentPage+=1;
                endOfResult=false;
            }*/
            for(int i=0;i<10;i++)
                arr.add("https://upload.wikimedia.org/wikipedia/commons/thumb/6/62/Starsinthesky.jpg/220px-Starsinthesky.jpg");
            return  arr;
        }

        @Override
        protected void onPostExecute(List<String> listItems) {
            loadingMore = false;
            if (listItems.size() > 0) {
                startIndex = startIndex + listItems.size();
                setItems(listItems);
            }
            super.onPostExecute(listItems);
        }

        private void setItems(List<String> listItems) {
            sitesArrayList.addAll(listItems);
            loadingMore=false;
            customAdapterForImageSearch.notifyDataSetChanged();
        }
    }
    /**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice searching...");
        startActivityForResult(intent, REQUEST_CODE);
    }
    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            final ArrayList < String > matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (!matches.isEmpty())
            {
                String Query = matches.get(0);
                editText.setText(Query);
                voiceSearch.setEnabled(true);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */ );
        urlConnection.setConnectTimeout(15000 /* milliseconds */ );
        urlConnection.setDoOutput(true);
        urlConnection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();
        System.out.println("JSON: " + jsonString);

        return new JSONObject(jsonString);
    }
    public void getResponse(
            int method,
            String url,
            JSONObject jsonValue,
            final VolleyCallback callback,final  String query,final String pageNumber) {
        StringRequest stringRequest =
                new StringRequest(
                        Request.Method.GET,
                        url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    callback.onSuccessResponse(response);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                NetworkResponse networkResponse = error.networkResponse;
                                String errorMessage = "Unknown error";
                                if (networkResponse == null) {
                                    if (error.getClass().equals(TimeoutError.class)) {
                                        errorMessage = "Request timeout";
                                    } else if (error.getClass().equals(NoConnectionError.class)) {
                                        errorMessage = "Failed to connect server";
                                    }
                                } else {
                                    String result = new String(networkResponse.data);
                                    try {
                                        JSONObject response = new JSONObject(result);
                                        String status = response.getString("status");
                                        String message = response.getString("message");

                                        Log.e("Error Status", status);
                                        Log.e("Error Message", message);

                                        if (networkResponse.statusCode == 404) {
                                            errorMessage = "Resource not found";
                                        } else if (networkResponse.statusCode == 500) {
                                            errorMessage = message+" Something is getting wrong";
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Log.i("Error", errorMessage);
                                error.printStackTrace();
                            }
                        }) {
                };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
    public boolean checkFieldsForEmptyValues(String editBoxText){
        if (TextUtils.isEmpty(editBoxText)) {
            editText.setError("Please enter Some thing to search for");
            editText.requestFocus();
            return false;
        }
        return true;
    }
}
