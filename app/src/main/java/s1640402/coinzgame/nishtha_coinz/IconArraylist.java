package s1640402.coinzgame.nishtha_coinz;

import java.util.ArrayList;

public class IconArraylist {
    private ArrayList<Integer> icons = new ArrayList<Integer>();

    public  IconArraylist(){

        //the quid icon is respective to the marker symbol number (0-9)
        icons.add(R.drawable.quid0);
        icons.add(R.drawable.quid1);
        icons.add(R.drawable.quid2);
        icons.add(R.drawable.quid3);
        icons.add(R.drawable.quid4);
        icons.add(R.drawable.quid5);
        icons.add(R.drawable.quid6);
        icons.add(R.drawable.quid7);
        icons.add(R.drawable.quid8);
        icons.add(R.drawable.quid9);

        //the dolr icon is respective to the marker symbol number (0-9) with a 1 before the number
        //hence dolr 0 will be 10 and dolr 1 will be 11
        icons.add(R.drawable.dolr0);
        icons.add(R.drawable.dolr1);
        icons.add(R.drawable.dolr2);
        icons.add(R.drawable.dolr3);
        icons.add(R.drawable.dolr4);
        icons.add(R.drawable.dolr5);
        icons.add(R.drawable.dolr6);
        icons.add(R.drawable.dolr7);
        icons.add(R.drawable.dolr8);
        icons.add(R.drawable.dolr9);


        //the shil icon is respective to the marker symbol number (0-9) with a 2 before the number
        //hence shil 0 will be 20 and shil 1 will be 21
        icons.add(R.drawable.shil0);
        icons.add(R.drawable.shil1);
        icons.add(R.drawable.shil2);
        icons.add(R.drawable.shil3);
        icons.add(R.drawable.shil4);
        icons.add(R.drawable.shil5);
        icons.add(R.drawable.shil6);
        icons.add(R.drawable.shil7);
        icons.add(R.drawable.shil8);
        icons.add(R.drawable.shil9);


        //the peny icon is respective to the marker symbol number (0-9) with a 3 before the number
        //hence peny 0 will be 30 and peny 1 will be 31
        icons.add(R.drawable.peny0);
        icons.add(R.drawable.peny1);
        icons.add(R.drawable.peny2);
        icons.add(R.drawable.peny3);
        icons.add(R.drawable.peny4);
        icons.add(R.drawable.peny5);
        icons.add(R.drawable.peny6);
        icons.add(R.drawable.peny7);
        icons.add(R.drawable.peny8);
        icons.add(R.drawable.peny9);

    }

    public ArrayList<Integer> geticons(){
        return icons;
    }

    public int geticonmarker(String curr, String sym) {

        int index = 0;

        if (curr.equals("QUID"))
        {
            index = Integer.parseInt(sym);
            return icons.get(index);
        }
        else if (curr.equals("DOLR")) {

            index = Integer.parseInt(sym) + 10;
            return icons.get(index);
        }
        else if (curr.equals("SHIL")) {

            index = Integer.parseInt(sym) + 20;
            return icons.get(index);
        }
        else{
            // if we reach here that means the currency has to be PENY
            index = Integer.parseInt(sym) + 30;
            return icons.get(index);
        }

    }
}
