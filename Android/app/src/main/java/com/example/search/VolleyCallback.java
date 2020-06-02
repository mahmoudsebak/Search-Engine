package com.example.search;

import org.json.JSONException;

public interface VolleyCallback {
    void onSuccessResponse(String result) throws JSONException;
}
