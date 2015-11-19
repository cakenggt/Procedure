package com.aleclownes.procedure;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by alownes on 10/27/2015.
 */
public class Checklist implements Serializable {

    protected Long parentId;
    protected Long id;
    protected int order;
    protected String title = "";
    protected List<ChecklistItem> items = new ArrayList<>();
    protected Date lastModified = new Date();
    protected boolean unSynced = true;

    public Checklist(){}

    public Checklist(JSONObject json){
        try {
            id = json.getLong("pk");
            order = json.getInt("order");
            try {
                parentId = json.getLong("parent");
            } catch (JSONException e){
                parentId = null;
            }
            lastModified = new Date(json.getLong("last_modified"));
            title = json.getString("title");
            JSONArray jsonItems = json.getJSONArray("items");
            for (int i = 0; i < jsonItems.length(); i++){
                items.add(new ChecklistItem(jsonItems.getJSONObject(i)));
            }
            unSynced = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isUnSynced() {
        return unSynced;
    }

    public void setUnSynced(boolean unSynced) {
        this.unSynced = unSynced;
    }
}
