package com.aleclownes.procedure;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**Checklist item in a checklist
 * Created by alownes on 10/27/2015.
 */
public class ChecklistItem implements Serializable {

    protected String text = "";
    private Long id;
    private int order;
    private boolean checkable = false;
    private boolean checked = false;

    public ChecklistItem(){}

    public ChecklistItem(JSONObject json){
        try {
            id = json.getLong("pk");
            checked = json.getBoolean("checked");
            checkable = json.getBoolean("checkable");
            text = json.getString("text");
            order = json.getInt("order");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isCheckable() {
        return checkable;
    }

    public void setCheckable(boolean checkable) {
        this.checkable = checkable;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
