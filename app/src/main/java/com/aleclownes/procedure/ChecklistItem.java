package com.aleclownes.procedure;

import java.io.Serializable;

/**Checklist item in a checklist
 * Created by alownes on 10/27/2015.
 */
public class ChecklistItem implements Serializable {

    protected String text = "";



    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
