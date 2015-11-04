package com.aleclownes.procedure;

import java.io.Serializable;

/**
 * Created by alownes on 10/28/2015.
 */
public class ChecklistEntry extends ChecklistItem implements Serializable {

    protected boolean checked;

    public ChecklistEntry(){
        super();
    }

    public ChecklistEntry(String text){
        super();
        this.text = text;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

}
