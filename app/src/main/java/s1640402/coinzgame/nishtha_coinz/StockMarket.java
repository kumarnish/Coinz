package s1640402.coinzgame.nishtha_coinz;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.time.LocalDate;


public class StockMarket extends AppCompatActivity {

    private String todaysratestring;
    private float[] todaysrate;
    private String[] prevdayrates;

    private float[] shilrates = new float[5];
    private float[] quidrates = new float[5];
    private float[] penyrates = new float[5];
    private float[] dolrrates = new float[5];

    private int[] dates = {LocalDate.now().minusDays(4).getDayOfMonth(),
                                 LocalDate.now().minusDays(3).getDayOfMonth(),
                                 LocalDate.now().minusDays(2).getDayOfMonth(),
                                 LocalDate.now().minusDays(1).getDayOfMonth(),
                                 LocalDate.now().getDayOfMonth()};

    private GraphView graph;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_market);
    }

    @Override
    public void onStart() {
        super.onStart();

        //get rates from the maps that were downloaded in the main menu
        Bundle bundle = getIntent().getExtras();
        todaysratestring = bundle.getString("exrates");
        prevdayrates = bundle.getStringArray("prevdaysrates");

        //get the numbers of the rates out of the json file
        todaysrate = getrates(todaysratestring);

        setupratearrays();
        setbuttonsup();
        settitlesup();

        //set up graph view and show peny as default
        graph = (GraphView) findViewById(R.id.graph);
        graph.setBackgroundColor(Color.BLACK);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(dates[0], penyrates[0]),
                new DataPoint(dates[1], penyrates[1]),
                new DataPoint(dates[2], penyrates[2]),
                new DataPoint(dates[3], penyrates[3]),
                new DataPoint(dates[4], penyrates[4]),
        });

        series.setThickness(8);
        series.setColor(Color.GRAY);

        //when points are clicked on the user can see that rate
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(StockMarket.this, "" + dataPoint.getY(), Toast.LENGTH_SHORT).show();
            }
        });

        graph.addSeries(series);
        graph.getGridLabelRenderer().setGridColor(Color.WHITE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.setTitle("PENY");
        graph.setTitleColor(Color.WHITE);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    //gets rates out of string
    public float[] getrates(String r){
        //create an array that separates each currency into an element of a string array
        String[] strrates = (r.substring(0,r.length()-2)).split(",");
        float[] rates = new float[4];
        String numstring;

        //the array has the rates in the order they are present in the geojson file
        // Shil, Dolr, Quid, Peny hence rates[0] is the rate of shil and etc..
        for (int i =0; i<strrates.length; i++) {
            numstring = strrates[i].substring(strrates[i].indexOf(":")+1);
            rates[i] = Float.parseFloat(numstring);
        }

        return rates;
    }

    //put each rate with respect to its currency in the approriate array
    public void setupratearrays(){
        float[] dayrates;

        for (int i =0; i<prevdayrates.length; i++) {
            dayrates = getrates(prevdayrates[i]);
            shilrates[i] = dayrates[0];
            dolrrates[i] = dayrates[1];
            quidrates[i] = dayrates[2];
            penyrates[i] = dayrates[3];
        }

        //add today's rates to the end
        shilrates[4] = todaysrate[0];
        dolrrates[4] = todaysrate[1];
        quidrates[4] = todaysrate[2];
        penyrates[4] = todaysrate[3];
    }

    //the button will have the percentage of change of the currencies
    //if negative then button will be red and green for when positive
    public void setbuttonsup() {
        //peny button
        Button penybtn = (Button)findViewById(R.id.penypercent);
        penybtn.setText("" + percentagecalculator(penyrates[3], penyrates[4]) + "%");
        penybtn.setBackgroundColor(colorselector(percentagecalculator(penyrates[3], penyrates[4])));

        //shil button
        Button shilbtn = (Button)findViewById(R.id.shilpercent);
        shilbtn.setText("" + percentagecalculator(shilrates[3], shilrates[4])+ "%");
        shilbtn.setBackgroundColor(colorselector(percentagecalculator(shilrates[3], shilrates[4])));

        //dolr button
        Button dolrbtn = (Button)findViewById(R.id.dolrpercent);
        dolrbtn.setText("" + percentagecalculator(dolrrates[3], dolrrates[4])+ "%");
        dolrbtn.setBackgroundColor(colorselector(percentagecalculator(dolrrates[3], dolrrates[4])));

        //quid button
        Button quidbtn = (Button)findViewById(R.id.quidpercent);
        quidbtn.setText("" + percentagecalculator(quidrates[3], quidrates[4])+ "%");
        quidbtn.setBackgroundColor(colorselector(percentagecalculator(quidrates[3], quidrates[4])));

    }

    //calculate percentage change of respective currency using day before and current day's rate
    public float percentagecalculator(float oldrate, float newrate){
        return (float)100.00*((newrate-oldrate)/oldrate);
    }

    //select color based on percentage being negative or positive
    public int colorselector(float percentage){
        if(percentage < 0)
            return Color.RED;
        else
            return Color.GREEN;
    }

    //when the peny percentage  button is clicked display the graph of it's rate for the past 4 days
    //and today's rate
    public void changegraphtoPENY(View view){

        graph.removeAllSeries();

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(dates[0], penyrates[0]),
                new DataPoint(dates[1], penyrates[1]),
                new DataPoint(dates[2], penyrates[2]),
                new DataPoint(dates[3], penyrates[3]),
                new DataPoint(dates[4], penyrates[4]),
        });

        graph.addSeries(series);
        series.setThickness(8);
        series.setColor(Color.GRAY);
        graph.setTitle("PENY");
        graph.setTitleColor(Color.WHITE);

    }

    //when the shil percentage  button is clicked display the graph of it's rate for the past 4 days
    //and today's rate
    public void changegraphtoSHIL(View view){

        graph.removeAllSeries();

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(dates[0], shilrates[0]),
                new DataPoint(dates[1], shilrates[1]),
                new DataPoint(dates[2], shilrates[2]),
                new DataPoint(dates[3], shilrates[3]),
                new DataPoint(dates[4], shilrates[4]),
        });

        graph.addSeries(series);
        series.setThickness(8);
        series.setColor(Color.GRAY);
        graph.setTitle("SHIL");
        graph.setTitleColor(Color.WHITE);

    }

    //when the quid percentage  button is clicked display the graph of it's rate for the past 4 days
    //and today's rate
    public void changegraphtoQUID(View view){

        graph.removeAllSeries();

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(dates[0], quidrates[0]),
                new DataPoint(dates[1], quidrates[1]),
                new DataPoint(dates[2], quidrates[2]),
                new DataPoint(dates[3], quidrates[3]),
                new DataPoint(dates[4], quidrates[4]),
        });

        graph.addSeries(series);
        series.setThickness(8);
        series.setColor(Color.GRAY);
        graph.setTitle("QUID");
        graph.setTitleColor(Color.WHITE);

    }

    //when the dolr percentage  button is clicked display the graph of it's rate for the past 4 days
    //and today's rate
    public void changegraphtoDOLR(View view){

        graph.removeAllSeries();

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(dates[0], dolrrates[0]),
                new DataPoint(dates[1], dolrrates[1]),
                new DataPoint(dates[2], dolrrates[2]),
                new DataPoint(dates[3], dolrrates[3]),
                new DataPoint(dates[4], dolrrates[4]),
        });

        graph.addSeries(series);
        series.setThickness(8);
        series.setColor(Color.GRAY);
        graph.setTitle("DOLR");
        graph.setTitleColor(Color.WHITE);

    }

    // add titles near button with today's rate next to them
    public void settitlesup(){

        TextView shiltitle = (TextView)findViewById(R.id.shiltitle);
        shiltitle.setText("SHIL - " + todaysrate[0]);

        TextView dolrtitle = (TextView)findViewById(R.id.dolrtitle);
        dolrtitle.setText("DOLR - " + todaysrate[1]);

        TextView quidtitle = (TextView)findViewById(R.id.quidtitle);
        quidtitle.setText("QUID - " + todaysrate[2]);

        TextView penytitle = (TextView)findViewById(R.id.penytitle);
        penytitle.setText("PENY - " + todaysrate[3]);
    }

    public void visitbank(View view){
        Intent intent = new Intent(this, Bank.class);
        startActivity(intent);
    }

}
