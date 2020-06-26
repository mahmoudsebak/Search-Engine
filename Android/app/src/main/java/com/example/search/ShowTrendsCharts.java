/*package com.example.search;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Random;

import okhttp3.internal.Util;

public class ShowTrendsCharts extends AppCompatActivity {
    private static final int MAX_X_VALUE = 10;
    private static final int MAX_Y_VALUE = 45;
    private static final int MIN_Y_VALUE = 5;
    private static final String SET_LABEL = "Average Temperature";
    private static final String[] DAYS = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };

    PieChart pieChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_trends_charts);
        pieChart =findViewById(R.id.fragment_verticalbarchart_chart);
        PieData data = createChartData();
        configureChartAppearance();
        prepareChartData(data);
    }

    private void configureChartAppearance() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5,10,5,5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(0.66f);
    }

    private PieData createChartData() {
        ArrayList<PieEntry> values = new ArrayList<>();
        Random random=new Random();
        values.add(new PieEntry(10,"mazen"));
        values.add(new PieEntry(10,"mazdwden"));
        values.add(new PieEntry(10,"mazedwn"));
        values.add(new PieEntry(10,"mazrrggen"));
        values.add(new PieEntry(10,"mazrgen"));
        values.add(new PieEntry(10,"mazgrgen"));
        values.add(new PieEntry(10,"mazehthen"));
        values.add(new PieEntry(10,"mazwqren"));
        values.add(new PieEntry(10,"mazrw3rween"));
        values.add(new PieEntry(10,"mazfefefen"));

        PieDataSet set1 = new PieDataSet(values, "words");
        set1.setSliceSpace(3f);
        set1.setSelectionShift(5f);
        set1.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData data =new PieData(set1);
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.BLACK);

        return data;
    }

    private void prepareChartData(PieData data) {
        data.setValueTextSize(12f);
        pieChart.setData(data);
        pieChart.invalidate();
    }
}*/

package com.example.search;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.Random;

import okhttp3.internal.Util;

public class ShowTrendsCharts extends AppCompatActivity {
    private static final int MAX_X_VALUE = 10;
    private static final int MAX_Y_VALUE = 50;
    private static final int MIN_Y_VALUE = 0;
    private static final String SET_LABEL = "Most trends words in"+MainActivity.region;
    public static  String[] DAYS ;

    HorizontalBarChart barChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_trends_charts);
        barChart =findViewById(R.id.fragment_verticalbarchart_chart);
        BarData data = createChartData();
        configureChartAppearance();
        prepareChartData(data);
    }

    private void configureChartAppearance() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawValueAboveBar(false);
        XAxis xAxis = barChart.getXAxis();
        DAYS=MainActivity.trendyWords.toArray(new String[0]);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return DAYS[(int) value];
            }
        });
    }

    private BarData createChartData() {
        ArrayList<BarEntry> values = MainActivity.trends;
        /*for (int i = 0; i < MAX_X_VALUE; i++) {
            float x = i;
            Random rand=new Random();
            float y = rand.nextFloat();
            values.add(new BarEntry(x, y));
        }*/
        BarDataSet set1 = new BarDataSet(values, SET_LABEL);
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);

        return data;
    }

    private void prepareChartData(BarData data) {
        data.setValueTextSize(12f);
        barChart.setData(data);
        barChart.invalidate();
    }
}
