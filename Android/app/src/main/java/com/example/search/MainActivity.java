package com.example.search;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1234;
    public static String url,title;
    AutoCompleteTextView editText;
    ImageButton voiceSearch;
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

        String[] COUNTRIES = new String[] {
                "Belgium", "France", "Italy", "Germany", "Spain"
        };
        ArrayAdapter<String> suggestionAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        editText.setAdapter(suggestionAdapter);

        ImageButton imageButton=(ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
    public boolean checkFieldsForEmptyValues(String editBoxText){
        if (TextUtils.isEmpty(editBoxText)) {
            editText.setError("Please enter Some thing to search for");
            editText.requestFocus();
            return false;
        }
        return true;
    }
}
