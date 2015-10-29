package com.aleclownes.procedure;

/**
 * Created by alownes on 10/27/2015.
 */
public class MasterChecklist extends Checklist {

    protected long parentId;

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }
}
