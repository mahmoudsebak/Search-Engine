package com.example.search;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SearchResult extends AppCompatActivity {
    int currentFirstVisibleItem = 0;
    int currentVisibleItemCount = 0;
    int totalItemCount = 0;
    int currentScrollState = 0;
    boolean loadingMore = false;
    Long startIndex = 0L;
    Long offset = 10L;
    View footerView;

    CustomAdapterForWebsiteList customAdapterForWebsiteList;
    ListView webSitesListView;
    ArrayList<WebSites> x;

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

        x =new ArrayList<WebSites>();
        webSitesListView=findViewById(R.id.websSteListView);
        footerView = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.base_list_item_loading_footer, null, false);

        WebSites webSite=new WebSites();
        webSite.setHeader("Google");
        webSite.setDescription("Google is best known search engine that serve billions of people every day");
        webSite.setUrl("https://www.google.com");
        x.add(webSite);

        WebSites webSite2=new WebSites();
        webSite2.setHeader("Youtube");
        webSite2.setDescription("Youtube is best known search engine for videos");
        webSite2.setUrl("https://www.youtube.com");
        x.add(webSite2);
        for(int i=0;i<7;i++){
            x.add(webSite);
        }
        customAdapterForWebsiteList=new CustomAdapterForWebsiteList(this,x);
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
            for(int i=0;i<7;i++){
                WebSites webSite=new WebSites();
                webSite.setHeader("Google"+i);
                webSite.setDescription("Google is best known search engine that serve billions of people every day");
                webSite.setUrl("https://www.google.com");
                arr.add(webSite);
                arr.add(webSite);
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
            x.addAll(listItems);
            loadingMore=false;
            customAdapterForWebsiteList.notifyDataSetChanged();
        }
    }
}



/*
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
    */
/**
     * Method for Checking Button Backgrounds
     *//*

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

    */
/**
     * Method for loading data in listview
     * @param number
     *//*

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
    }*/
