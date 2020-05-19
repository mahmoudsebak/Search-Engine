package com.example.search;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchResult extends AppCompatActivity {
    CustomAdapterForWebsiteList customAdapterForWebsiteList;
    ListView webSitesListView;
    ArrayList<WebSites> webSites;

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

        webSites =new ArrayList<WebSites>();
        webSitesListView=findViewById(R.id.websSteListView);

        //Mimic Real Data
        WebSites webSite=new WebSites();
        webSite.setHeader("Google");
        webSite.setDescription("Google is best known search engine that serve billions of people every day");
        webSite.setUrl("https://www.google.com");
        webSites.add(webSite);

        WebSites webSite2=new WebSites();
        webSite2.setHeader("Youtube");
        webSite2.setDescription("Youtube is best known search engine for videos");
        webSite2.setUrl("https://www.youtube.com");
        webSites.add(webSite2);
        webSites.add(webSite2);
        webSites.add(webSite2);
        webSites.add(webSite2);
        webSites.add(webSite2);
        webSites.add(webSite2);
        customAdapterForWebsiteList=new CustomAdapterForWebsiteList(this, webSites);
        webSitesListView.setAdapter(customAdapterForWebsiteList);

    }
}
