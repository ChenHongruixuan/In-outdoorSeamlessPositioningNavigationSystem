package data;

public class WifiDataContainer {
    private String[] bssidArray;
    private Float[] levelArray;

    public WifiDataContainer(String[] bssidArray, Float[] levelArray) {
        this.bssidArray = bssidArray;
        this.levelArray = levelArray;
    }

    public Float[] getLevelArray() {
        return levelArray;
    }

    public String[] getBssidArray() {
        return bssidArray;
    }
}
