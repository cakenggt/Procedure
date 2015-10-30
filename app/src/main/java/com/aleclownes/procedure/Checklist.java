package com.aleclownes.procedure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Checklist abstract class
 * Created by alownes on 10/27/2015.
 */
public abstract class Checklist implements Serializable {

    protected long id;
    protected String title = "";
    protected List<ChecklistItem> items = new ArrayList<ChecklistItem>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ChecklistItem> getItems() {
        return items;
    }

    public void setItems(List<ChecklistItem> items) {
        this.items = items;
    }

}
