package com.aleclownes.procedure;

import java.io.Serializable;

/**
 * Created by alownes on 10/27/2015.
 */
public class MasterChecklist extends Checklist implements Serializable {

    protected long parentId;

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }
}
