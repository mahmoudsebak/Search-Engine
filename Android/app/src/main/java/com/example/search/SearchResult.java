package com.example.search;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchResult extends AppCompatActivity {
    CustomAdapterForWebsiteList customAdapterForWebsiteList;
    ListView webSitesListView;
    ArrayList<WebSites> webSites;

    public int TOTAL_LIST_ITEMS = 1030;
    public int NUM_ITEMS_PAGE   = 10;
    private int noOfBtns;
    private Button[] btns;

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
        for(int i=0;i<50;i++){
            webSites.add(webSite);
        }
        TOTAL_LIST_ITEMS=webSites.size();
        ButtonListConfigure();
        loadList(0);
        CheckButBackGround(0);
    }
    private void ButtonListConfigure()
    {
        int val = TOTAL_LIST_ITEMS%NUM_ITEMS_PAGE;
        val = val==0?0:1;
        noOfBtns=TOTAL_LIST_ITEMS/NUM_ITEMS_PAGE+val;

        LinearLayout ll = findViewById(R.id.btnLay);

        btns  = new Button[noOfBtns];

        for(int i=0;i<noOfBtns;i++)
        {
            btns[i] =   new Button(this);
            btns[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
            btns[i].setText(""+(i+1));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            ll.addView(btns[i], lp);

            final int j = i;
            btns[j].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    loadList(j);
                    CheckButBackGround(j);
                }
            });
        }

    }
    /**
     * Method for Checking Button Backgrounds
     */
    private void CheckButBackGround(int index)
    {
        for(int i=0;i<noOfBtns;i++)
        {
            if(i==index)
            {
                btns[index].setBackgroundDrawable(getResources().getDrawable(R.drawable.box_green));
                btns[i].setTextColor(getResources().getColor(android.R.color.white));
            }
            else
            {
                btns[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
                btns[i].setTextColor(getResources().getColor(android.R.color.black));
            }
        }

    }

    /**
     * Method for loading data in listview
     * @param number
     */
    private void loadList(int number)
    {
        ArrayList<WebSites> sort = new ArrayList<WebSites>();

        int start = number * NUM_ITEMS_PAGE;
        for(int i=start;i<(start)+NUM_ITEMS_PAGE;i++)
        {
            if(i<webSites.size())
            {
                sort.add(webSites.get(i));
            }
            else
            {
                break;
            }
        }
        customAdapterForWebsiteList=new CustomAdapterForWebsiteList(this, sort);
        webSitesListView.setAdapter(customAdapterForWebsiteList);
    }
}
