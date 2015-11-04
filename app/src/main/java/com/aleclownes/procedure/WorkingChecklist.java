package com.aleclownes.procedure;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by alownes on 10/27/2015.
 */
public class WorkingChecklist extends Checklist implements Serializable {

    public WorkingChecklist(){
        super();
    }

    public WorkingChecklist(MasterChecklist master){
        super.items = new ArrayList<ChecklistItem>();
        for (ChecklistItem item : master.items){
            if (item instanceof ChecklistHeader){
                items.add(item);
            }
            else if (item instanceof ChecklistEntry){
                items.add(new WorkingChecklistEntry((ChecklistEntry)item));
            }
        }
        super.title = master.title;
    }

}
