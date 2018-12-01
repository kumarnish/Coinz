package s1640402.coinzgame.nishtha_coinz;

//class for the storing of coins on the database
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

