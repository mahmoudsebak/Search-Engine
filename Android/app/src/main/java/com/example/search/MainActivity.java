package com.example.search;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1234;
    public static String region;
    AutoCompleteTextView editText;
    ImageButton voiceSearch;
    Spinner spinner;
    public static ArrayList<BarEntry> trends;
    public static ArrayList<String> trendyWords;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_cyan));

        editText=( AutoCompleteTextView)findViewById(R.id.editText);
        loadSuggestions();

        // Spinner element
        spinner = (Spinner) findViewById(R.id.region_spinner);

        //Add countries as Spinner elements
        SortedSet<String> countries = new TreeSet<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            if (!TextUtils.isEmpty(locale.getDisplayCountry())) {
                countries.add(locale.getDisplayCountry());
            }
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countries.toArray(new String[0]));

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        // Spinner click listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                region=spinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                // sometimes you need nothing here
            }
        });

        ImageButton showTrend=findViewById(R.id.view_charts);
        showTrend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getResponse(
                        Request.Method.GET,
                        ULRConnection.url+"/search/trends?region="+region,
                        null,
                        new VolleyCallback() {
                            @Override
                            public void onSuccessResponse(String response) throws JSONException {
                                trendyWords=new ArrayList<>();
                                trends=new ArrayList<>();
                                JSONObject obj= new JSONObject(response);;
                                try {
                                    if(obj.getJSONArray("result").length()!=0){
                                        JSONArray trendsResult = obj.getJSONArray("result");
                                        for(int i=0;i<trendsResult.length();i++) {
                                            JSONObject current = trendsResult.getJSONObject(i);
                                            trends.add(new BarEntry(i, current.getInt("count")));
                                            trendyWords.add(current.getString("person"));
                                        }
                                        Intent i=new Intent(getApplicationContext(),ShowTrendsCharts.class);
                                        startActivity(i);
                                    }else
                                        Toast.makeText(getApplicationContext(),"No result found",Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    if(obj.isNull("result"))
                                        Toast.makeText(getApplicationContext(),"No result found",Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        },editText.getText().toString(),"1");
            }
        });

        ImageButton imageButton=(ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread sendQuery=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getResponse(
                                Request.Method.POST,
                                ULRConnection.url+"/search/addSuggestion?",
                                null,
                                new VolleyCallback() {
                                    @Override
                                    public void onSuccessResponse(String response) throws JSONException {
                                        JSONObject obj= new JSONObject(response);
                                        loadSuggestions();
                                    }
                                },editText.getText().toString(),"");}
                });
                sendQuery.start();
                if(checkFieldsForEmptyValues(editText.getText().toString())){
                    String editTextString=editText.getText().toString();
                    editTextString=editTextString.replaceAll("\\s+","");
                    getResponse(
                            Request.Method.GET,
                            ULRConnection.url+"/search/query?query="+editTextString+"&page="+"1",
                            null,
                            new VolleyCallback() {
                                @Override
                                public void onSuccessResponse(String response) throws JSONException {
                                    JSONObject obj= new JSONObject(response);;
                                    try {
                                        if(obj.getJSONArray("result").length()!=0){
                                            ArrayList<WebSites>webSitesArrayList=new ArrayList<>();
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
                                                webSitesArrayList.add(currentWebsite);
                                            }
                                            Intent i=new Intent(MainActivity.this,SearchResult.class);
                                            i.putParcelableArrayListExtra("searchResult", (ArrayList<? extends Parcelable>) webSitesArrayList);
                                            i.putExtra("TypedWord",editText.getText().toString());
                                            startActivity(i);
                                        }else
                                            Toast.makeText(getApplicationContext(),"No result found",Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        if(obj.isNull("result"))
                                            Toast.makeText(getApplicationContext(),"No result found",Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }
                                }
                            },editText.getText().toString(),"1");
                }
            }
        });
        voiceSearch=(ImageButton) findViewById(R.id.search_voice_btn);
        voiceSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });
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
                        method,
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
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("suggestion", query);
                        params.put("region",region);
                        return params;
                    }
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
    public void loadSuggestions(){
        ArrayList<String> searchArrayList= new ArrayList<String>();
        getResponse(
                Request.Method.GET,
                ULRConnection.url+"/search/getSuggestions?",
                null,
                new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) throws JSONException {
                        JSONObject obj= new JSONObject(response);;
                        try {
                            if(obj.getJSONArray("result").length()!=0){
                                ArrayList<WebSites>webSitesArrayList=new ArrayList<>();
                                // converting response to json object
                                // if no error in response
                                // getting the result from the response
                                JSONArray searchResult = obj.getJSONArray("result");
                                for(int i=0;i<searchResult.length();i++) {
                                    searchArrayList.add(searchResult.getString(i));
                                }
                            }else
                                Toast.makeText(getApplicationContext(),"No Suggestion Found",Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            if(obj.isNull("result"))
                                Toast.makeText(getApplicationContext(),"No Suggestion Found",Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                        AutoCompleteAdapter adapter = new AutoCompleteAdapter(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, android.R.id.text1, searchArrayList);
                        editText.setAdapter(adapter);
                        editText.setThreshold(1);
                    }
                },editText.getText().toString(),"1");

    }
}
