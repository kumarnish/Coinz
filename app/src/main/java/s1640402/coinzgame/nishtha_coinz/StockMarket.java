package s1640402.coinzgame.nishtha_coinz;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/* =====================================STOCK MARKET VIEW=======================================
In this activity the user can:
-Can see the graph of the rates of each of the currencies for the past 4 days and today's
* */
public class StockMarket extends AppCompatActivity {

    private float[] todaysrate; //stores current day's exchange rate
    private String[] prevdayrates; //stores exchange rates of previous 4 days

    //stores respective currency's 5 day rates
    private float[] shilrates = new float[5];
    private float[] quidrates = new float[5];
    private float[] penyrates = new float[5];
    private float[] dolrrates = new float[5];

    //stores past 5 days date
    private Date[] dates = new Date[5];

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
        String todaysratestring = bundle.getString("exrates");
        prevdayrates = bundle.getStringArray("prevdaysrates");

        //get the numbers of the rates out of the json file
        todaysrate = (new ConverterandDialogs()).getrates(todaysratestring);

        //set up the rate arrays for each of the currencies
        setupratearrays();
        //set the values and colors of the buttons based on if the currency went up or down
        setbuttonsup();
        //set up the text near the buttons with todays rate and the title of each currency
        settitlesup();

        //set up the bottom labels which are the date from the previous for days and today's date
        try {
            setdatesarray();
        } catch (Exception e) {
            e.printStackTrace();
        }


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

        series.setThickness(10);
        series.setColor(Color.BLACK);

        //format date for graph labels
        dateformatter();
        //configure graph's layout
        graph.getGridLabelRenderer().setHumanRounding(false);
        graph.addSeries(series);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
        graph.setTitle("PENY");
        graph.setTitleColor(Color.BLACK);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.setBackgroundColor(android.graphics.Color.rgb(240,252,255));
    }

    //create arrays for each currency's rates
    public void setupratearrays(){
        float[] dayrates;
        for (int i =0; i<prevdayrates.length; i++) {
            //get rates for specific day and then put them in their currencies arrays
            dayrates = (new ConverterandDialogs()).getrates(prevdayrates[i]);
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
        penybtn.setBackgroundColor(Color.parseColor(colorselector(percentagecalculator(penyrates[3], penyrates[4]))));

        //shil button
        Button shilbtn = (Button)findViewById(R.id.shilpercent);
        shilbtn.setText("" + percentagecalculator(shilrates[3], shilrates[4])+ "%");
        shilbtn.setBackgroundColor(Color.parseColor(colorselector(percentagecalculator(shilrates[3], shilrates[4]))));

        //dolr button
        Button dolrbtn = (Button)findViewById(R.id.dolrpercent);
        dolrbtn.setText("" + percentagecalculator(dolrrates[3], dolrrates[4])+ "%");
        dolrbtn.setBackgroundColor(Color.parseColor(colorselector(percentagecalculator(dolrrates[3], dolrrates[4]))));

        //quid button
        Button quidbtn = (Button)findViewById(R.id.quidpercent);
        quidbtn.setText("" + percentagecalculator(quidrates[3], quidrates[4])+ "%");
        quidbtn.setBackgroundColor(Color.parseColor(colorselector(percentagecalculator(quidrates[3], quidrates[4]))));

    }

    //calculate percentage change of respective currency using day before and current day's rate
    public float percentagecalculator(float oldrate, float newrate){
        return (float)100.00*((newrate-oldrate)/oldrate);
    }

    //select color based on percentage being negative or positive
    public String colorselector(float percentage){
        if(percentage < 0)
            return "#EA0607";
        else
            return "#01A101";
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

    //when either button is pressed the respective currencies graph will show
    public void changegraphtoPENY(View view){
        graphchanger(penyrates,"PENY");
    }

    public void changegraphtoSHIL(View view){
        graphchanger(shilrates,"SHIL");
    }

    public void changegraphtoQUID(View view) { graphchanger(quidrates,"QUID"); }

    public void changegraphtoDOLR(View view){
        graphchanger(dolrrates,"DOLR");
    }

    //changes graph view to which ever currency button calls it
    public void graphchanger(float[] rates, String curr) {
        graph.removeAllSeries();

        DataPoint starting =  new DataPoint(dates[0], dolrrates[0]);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(dates[0], rates[0]),
                new DataPoint(dates[1], rates[1]),
                new DataPoint(dates[2], rates[2]),
                new DataPoint(dates[3], rates[3]),
                new DataPoint(dates[4], rates[4]),
        });

        dateformatter();
        graph.addSeries(series);
        series.setThickness(10);
        series.setColor(Color.GRAY);
        graph.setTitle(curr);
        graph.setTitleColor(Color.BLACK);
        graph.setBackgroundColor(android.graphics.Color.rgb(240,252,255));
    }


    //set up array that contains will the dates of the past 4 days and todays date
    public void setdatesarray() throws Exception {
        dates[0] = getNewDate(new Date(),-4);
        dates[1] = getNewDate(new Date(),-3);
        dates[2] = getNewDate(new Date(),-2);
        dates[3] = getNewDate(new Date(),-1);
        dates[4] = new Date();
    }

    //gets you the days before a specfic day
    public static Date getNewDate(Date inputDate, int days) throws Exception {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(inputDate);
        calendar.add(Calendar.DAY_OF_MONTH, days);

        return calendar.getTime();
    }

    //formats the date from the timestamp form to an shorter form for the graph labels
    public void dateformatter() {
        // custom label formatter to show currency "EUR"
        SimpleDateFormat format = new SimpleDateFormat("dd//MM");
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return format.format(value);
                }
                    return null;

            }
        });

    }

    //goes back to main menu
    public void backtomain(View view) {
        Intent intent = new Intent(this, MainView.class);
        startActivity(intent);
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
}
