package olddata;

public class MagContainer {
    private static double magModel = 0.0;

    public MagContainer(double magModel) {
        MagContainer.magModel = magModel;
    }

    public static void setMagModel(double newMagModel) {
        MagContainer.magModel = newMagModel;
    }

    public static void setMagModel(String newMagModelString) {
        MagContainer.magModel = Double.valueOf(newMagModelString);
    }

    public static double getMagModel() {
        return MagContainer.magModel;
    }

    public String toString() {
        return String.valueOf(magModel);
    }
}
