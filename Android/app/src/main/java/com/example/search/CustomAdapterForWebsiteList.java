package com.example.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
        // set Website Description
        TextView description = (TextView) listItem.findViewById(R.id.textView2);
        description.setText(currentWebsite.getDescription());
        //Click listener to enter website
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"You entered Website",Toast.LENGTH_LONG).show();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(currentWebsite.getUrl()));
                context.startActivity(i);
            }
        });

        return listItem;
    };
}
