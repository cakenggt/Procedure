package com.aleclownes.procedure;

import java.io.Serializable;

/**Checklist item in a checklist
 * Created by alownes on 10/27/2015.
 */
public class ChecklistItem implements Serializable {

    protected String text = "";
    private boolean checkable = false;
    private boolean checked = false;

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

}
