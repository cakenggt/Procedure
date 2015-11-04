package com.aleclownes.procedure;

import java.io.Serializable;

/**
 * Created by alownes on 10/28/2015.
 */
public class WorkingChecklistEntry extends ChecklistEntry implements Serializable {

    protected boolean checked;

    public WorkingChecklistEntry(ChecklistEntry master){
        super(master.text);
        this.checked = false;
    }

    public WorkingChecklistEntry(){
        super();
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

}
