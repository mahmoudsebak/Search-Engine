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
import android.os.Parcelable;
import android.speech.RecognizerIntent;
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

import com.android.volley.AuthFailureError;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SearchResult extends AppCompatActivity {
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

        ImageButton search=findViewById(R.id.imageButton1);
        voiceSearch=findViewById(R.id.search_voice_btn1);
        voiceSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });
        editText=findViewById(R.id.editText1);
        editText.setText(getIntent().getStringExtra("TypedWord"));
        /*WebSites webSite=new WebSites();
        webSite.setHeader("Google");
        webSite.setDescription("Google is best known search engine that serve billions of people every day");
        webSite.setUrl("https://www.google.com");
        sitesArrayList.add(webSite);

        WebSites webSite2=new WebSites();
        webSite2.setHeader("Youtube");
        webSite2.setDescription("Youtube is best known search engine for videos");
        webSite2.setUrl("https://www.youtube.com");
        sitesArrayList.add(webSite2);
        for(int i=0;i<7;i++){
            sitesArrayList.add(webSite);
        }*/
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
            super.onPreExecute();
        }

        @Override
        protected List<WebSites> doInBackground(Void... voids) {

            return getNextItems(startIndex, offset);
        }

        private List<WebSites> getNextItems(Long startIndex, Long offset) {

            //Mimic Real Data
            ArrayList<WebSites>arr=new ArrayList<>();
            currentPage+=1;
            getResponse(
                    Request.Method.GET,
                    "http://192.168.1.14:8080/search/query?query="+editText.getText().toString()+"&page="+ currentPage,
                    null,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessResponse(String response) {
                            try {
                                WebSites currentWebsite=new WebSites();
                                // converting response to json object
                                JSONObject obj = new JSONObject(response);
                                // if no error in response
                                // getting the result from the response
                                JSONArray searchResult = obj.getJSONArray("result");
                                for(int i=0;i<searchResult.length();i++) {
                                    JSONObject current = searchResult.getJSONObject(i);
                                    currentWebsite.setUrl(current.getString("url"));
                                    Document document= Jsoup.connect(current.getString("url")).get();
                                    currentWebsite.setDescription(current.getString("content"));
                                    currentWebsite.setHeader(document.title());
                                    arr.add(currentWebsite);
                                }
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    },editText.getText().toString(),Integer.toString(currentPage));
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
                                callback.onSuccessResponse(response);
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
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("query", query);
                        params.put("page", pageNumber);
                        return params;
                    }
                };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
