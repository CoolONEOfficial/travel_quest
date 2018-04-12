package ru.coolone.travelquest.ui.adapters;

/**
 * Created by coolone on 15.11.17.
 */

public class BaseSectionedHeader {
    public BaseSectionedHeader(String title) {
        this.title = title;
    }

    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
