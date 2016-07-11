package it.sasabz.android.sasabus.model;

public class Changelog {
    private final String title;
    private final String changes;

    public Changelog(String title, String changes) {
        this.title = title;
        this.changes = changes;
    }

    public CharSequence getTitle() {
        return title;
    }

    public CharSequence getChanges() {
        return changes;
    }
}