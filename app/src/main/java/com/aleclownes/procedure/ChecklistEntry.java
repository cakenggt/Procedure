package com.aleclownes.procedure;

import java.io.Serializable;

/**An entry in the checklist. Different from a header in that a working checklist entry can
 * be checked
 * Created by alownes on 10/28/2015.
 */
public class ChecklistEntry extends ChecklistItem implements Serializable {

    public ChecklistEntry(){
        this("");
    }

    public ChecklistEntry(String text){
        this.text = text;
    }

}
