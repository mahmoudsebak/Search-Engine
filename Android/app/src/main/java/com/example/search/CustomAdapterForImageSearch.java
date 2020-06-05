package com.example.search;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomAdapterForImageSearch extends ArrayAdapter {
    private ArrayList<ImageClass> webSitesArrayList;
    private final Activity context;
    public CustomAdapterForImageSearch(Activity context, ArrayList<ImageClass> list) {
        super(context, R.layout.img_result_view, list);
        this.context = context;
        webSitesArrayList = list;
    }
    public View getView(final int position, View view, ViewGroup parent) {
        View listItem = view;
        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.img_result_view, parent, false);
        final ImageClass currentWebsite = webSitesArrayList.get(position);
        final View post=listItem;

        // set Website Image
        ImageView resultImage = listItem.findViewById(R.id.image_result);
        String imageURL;
        if(currentWebsite.getImgSource().equals(""))
            imageURL="https://image.shutterstock.com/z/stock-vector-default-ui-image-placeholder-for-wireframes-for-apps-and-websites-1037719192.jpg";
        else
            imageURL=currentWebsite.getImgSource();
        Picasso.get().load(imageURL).resize(100,100).placeholder(R.drawable.ic_broken_image_black_24dp).into(resultImage);
        resultImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"You entered Website",Toast.LENGTH_LONG).show();
                Thread sendURL=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getResponse(
                                Request.Method.GET,
                                ULRConnection.url+"/search/user_action?"+currentWebsite.getUrl(),
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
