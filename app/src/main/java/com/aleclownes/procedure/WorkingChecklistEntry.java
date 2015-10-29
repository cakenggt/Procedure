package com.aleclownes.procedure;

/**
 * Created by alownes on 10/28/2015.
 */
public class WorkingChecklistEntry extends ChecklistEntry {

    protected boolean checked;

    public WorkingChecklistEntry(ChecklistEntry master){
        super(master.text);
        this.checked = false;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

}
