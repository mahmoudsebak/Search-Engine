package com.example.search;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(currentWebsite.getUrl()));
                context.startActivity(i);
            }
        });
        return listItem;
    };
}
