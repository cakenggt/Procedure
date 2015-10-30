package com.aleclownes.procedure;

import java.io.Serializable;

/**Header in a checklist
 * Created by alownes on 10/28/2015.
 */
public class ChecklistHeader extends ChecklistItem implements Serializable {

    public ChecklistHeader(){
        this("");
    }

    public ChecklistHeader(String text){
        this.text = text;
    }

}
