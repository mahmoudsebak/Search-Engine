package com.example.search;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SearchResult extends AppCompatActivity {
    private static final int REQUEST_CODE = 1234;
    boolean loadingMore = false;
    Long startIndex = 0L;
    Long offset = 10L;
    View footerView;
    ImageButton voiceSearch;
    AutoCompleteTextView editText;

    CustomAdapterForWebsiteList customAdapterForWebsiteList;
    ListView webSitesListView;
    ArrayList<WebSites> x;

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
}
