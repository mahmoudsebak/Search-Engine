package com.example.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomAdapterForWebsiteList extends ArrayAdapter {
    private ArrayList<WebSites> webSitesArrayList;
    private final Activity context;
    public CustomAdapterForWebsiteList(Activity context, ArrayList<WebSites> list) {
        super(context, R.layout.website_list_view, list);
        this.context = context;
        webSitesArrayList = list;
    }
    public View getView(final int position, View view, ViewGroup parent) {
        View listItem = view;
        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.website_list_view, parent, false);
        final WebSites currentWebsite = webSitesArrayList.get(position);
        final View post=listItem;

        // set Website title
        TextView title = (TextView) listItem.findViewById(R.id.textView);
        title.setText(currentWebsite.getHeader());

        //Set url
        TextView url = (TextView) listItem.findViewById(R.id.url);
        url.setText(currentWebsite.getUrl());

        // set Website Description
        TextView description = (TextView) listItem.findViewById(R.id.textView2);
        description.setText(currentWebsite.getDescription());
        //Click listener to enter website
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"You entered Website",Toast.LENGTH_LONG).show();
                Thread sendURL=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getResponse(
                                Request.Method.GET,
                                ULRConnection.url+"/search/userAction?url="+currentWebsite.getUrl(),
                                null,
                                new VolleyCallback() {
                                    @Override
                                    public void onSuccessResponse(String response) throws JSONException {
                                        JSONObject obj= new JSONObject(response);; }
                                },"","1");
                    }
                });
                sendURL.start();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(currentWebsite.getUrl()));
                context.startActivity(i);
            }
        });

        return listItem;
    };
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
                        return params;
                    }
                };
        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }
}
