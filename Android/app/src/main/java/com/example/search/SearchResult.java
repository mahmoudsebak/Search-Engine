package com.example.search;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
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
import java.util.Objects;

public class SearchResult extends AppCompatActivity {
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

    CustomAdapterForWebsiteList customAdapterForWebsiteList;
    ListView webSitesListView;
    ArrayList<WebSites> sitesArrayList;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_cyan));

        sitesArrayList =new ArrayList<WebSites>();
        sitesArrayList.addAll(Objects.requireNonNull(getIntent().getParcelableArrayListExtra("searchResult")));
        webSitesListView=findViewById(R.id.websSteListView);
        footerView = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.base_list_item_loading_footer, null, false);
        TextView textResult=findViewById(R.id.text_result);
        TextView imageResult=findViewById(R.id.image_result);
        imageResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getApplicationContext(),ImageResultSearch.class);
                i.putExtra("toImage",editText.getText().toString());
                startActivity(i);
            }
        });

        ImageButton search=findViewById(R.id.imageButton1);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingMore=false;
                endOfResult=false;
                currentPage=1;
                if(checkFieldsForEmptyValues(editText.getText().toString())){
                    String editTextString=editText.getText().toString();
                    editTextString=editTextString.replaceAll("\\s+","");
                    getResponse(
                            Request.Method.GET,
                            ULRConnection.url+"/search/query?query="+editTextString+"&img=0"+"&page="+ 1,
                            null,
                            new VolleyCallback() {
                                @Override
                                public void onSuccessResponse(String response) throws JSONException {
                                    JSONObject obj = new JSONObject(response);
                                    try {
                                        if(obj.getJSONArray("result").length()!=0){
                                            sitesArrayList.clear();
                                            // converting response to json object
                                            // if no error in response
                                            // getting the result from the response
                                            JSONArray searchResult = obj.getJSONArray("result");
                                            for(int i=0;i<searchResult.length();i++) {
                                                WebSites currentWebsite=new WebSites();
                                                JSONObject current = searchResult.getJSONObject(i);
                                                currentWebsite.setUrl(current.getString("url"));
                                                currentWebsite.setDescription(current.getString("content"));
                                                currentWebsite.setHeader(current.getString("title"));
                                                sitesArrayList.add(currentWebsite);
                                            }
                                            customAdapterForWebsiteList.notifyDataSetChanged();
                                            webSitesListView.setSelection(0);
                                            endOfResult= sitesArrayList.size() == 0;
                                        }else
                                            Toast.makeText(getApplicationContext(),"No result found",Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        if(obj.isNull("result"))
                                            Toast.makeText(getApplicationContext(),"No result found",Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }
                                }
                            },editText.getText().toString(),Integer.toString(currentPage));
                }
            }
        });
        voiceSearch=findViewById(R.id.search_voice_btn1);
        voiceSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });
        editText=findViewById(R.id.editText1);
        editText.setText(getIntent().getStringExtra("TypedWord"));
        customAdapterForWebsiteList=new CustomAdapterForWebsiteList(this, sitesArrayList);
        webSitesListView.setAdapter(customAdapterForWebsiteList);
        webSitesListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
                {
                    if(!loadingMore)
                    {
                        loadingMore = true;
                        new LoadMoreItemsTask((Activity) view.getContext()).execute();
                        webSitesListView.setSelection(firstVisibleItem);
                    }

                }
            }
        });

    }
    private class LoadMoreItemsTask extends AsyncTask<Void, Void, List<WebSites>> {

        private Activity activity;
        private View footer;

        private LoadMoreItemsTask(Activity activity) {
            this.activity = (Activity) activity;
            loadingMore = true;
            footer = ((Activity) activity).getLayoutInflater().inflate(R.layout.base_list_item_loading_footer, null);
        }

        @Override
        protected void onPreExecute() {
            webSitesListView.addFooterView(footer);
            if(endOfResult){
                webSitesListView.removeFooterView(footer);
                loadingMore=true;
                Toast.makeText(getApplicationContext(),"No more result",Toast.LENGTH_LONG).show();
            }else {
                webSitesListView.addFooterView(footer);
                loadingMore=false;
            }
            super.onPreExecute();
        }

        @Override
        protected List<WebSites> doInBackground(Void... voids) {

            try {
                return getNextItems(startIndex, offset);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private List<WebSites> getNextItems(Long startIndex, Long offset) throws IOException, JSONException {
            ArrayList<WebSites>arr=new ArrayList<>();
            try {
                // converting response to json object
                JSONObject obj = getJSONObjectFromURL(ULRConnection.url+"/search/query?query="+editText.getText().toString()+"&img=0"+"&page="+ (currentPage+1));
                // if no error in response
                // getting the result from the response
                JSONArray searchResult = obj.getJSONArray("result");
                for(int i=0;i<searchResult.length();i++) {
                    WebSites currentWebsite=new WebSites();
                    JSONObject current = searchResult.getJSONObject(i);
                    currentWebsite.setUrl(current.getString("url"));
                    currentWebsite.setDescription(current.getString("content"));
                    currentWebsite.setHeader(current.getString("title"));
                    arr.add(currentWebsite);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(arr.size()==0)
                endOfResult=true;
            else{
                currentPage+=1;
                endOfResult=false;
            }
            return  arr;
        }

        @Override
        protected void onPostExecute(List<WebSites> listItems) {
            if (footer != null) {
                webSitesListView.removeFooterView(footer);
            }
            loadingMore = false;
            if (listItems.size() > 0) {
                startIndex = startIndex + listItems.size();
                setItems(listItems);
            }
            super.onPostExecute(listItems);
        }

        private void setItems(List<WebSites> listItems) {
            sitesArrayList.addAll(listItems);
            loadingMore=false;
            customAdapterForWebsiteList.notifyDataSetChanged();
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