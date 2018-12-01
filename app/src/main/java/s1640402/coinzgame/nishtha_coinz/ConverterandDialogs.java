package s1640402.coinzgame.nishtha_coinz;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

//class to store the methods used to convert rate strings to arrays and convert coins to gold
//and create the standard dialog box  many classes use the same methods
public class ConverterandDialogs {

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

    public double currconverter(String rate, String coin) {
        //convert rates to an array
        float[] ratestoday = getrates(rate);

        //multiple coins by their respective currencies gold value on the current day
        if (coin.contains("SHIL"))
            //the substring function retrieves it from the string in listview, ex: "6.005392773625784 DOLR"
            return ratestoday[0]*Double.parseDouble(coin.substring(0,coin.length()-5));
        else if (coin.contains("DOLR"))
            return ratestoday[1]*Double.parseDouble(coin.substring(0,coin.length()-5));
        else if (coin.contains("QUID"))
            return ratestoday[2]*Double.parseDouble(coin.substring(0,coin.length()-5));
        else
            return ratestoday[3]*Double.parseDouble(coin.substring(0,coin.length()-5));

    }

    //create standard alert dialog
    public AlertDialog OKdialog(String message, String title, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        return builder.create();
    }
}
