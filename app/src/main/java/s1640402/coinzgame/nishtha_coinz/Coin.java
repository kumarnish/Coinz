package s1640402.coinzgame.nishtha_coinz;

//class used to create the coin object to store in the firebase database
public class Coin {

    private String id;
    private String value;
    private String currency;

    public Coin(String i, String v, String c) {
        id = i;
        value = v;
        currency = c;
    }

    public String getId(){
        return id;
    }

    public String getValue(){
        return value;
    }

    public  String getCurrency(){
        return currency;
    }
}

