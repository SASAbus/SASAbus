package it.sasabz.android.sasabus.model.route;

public class RouteLegend {
    private final int type;
    private final String name;
    private final String color;

    public RouteLegend(int type, String name, String color) {
        this.type = type;
        this.name = name;
        this.color = color;
    }

    public int getType() {
        return type;
    }

    public CharSequence getName() {
        return name;
    }

    public String getColor() {
        return color;
    }
}