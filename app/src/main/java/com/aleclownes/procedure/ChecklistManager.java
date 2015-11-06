package com.aleclownes.procedure;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by alownes on 10/29/2015.
 */
public interface ChecklistManager {

    public static final String CHECKLIST_LIST_FILENAME = "checklists.bin";

    public long create(Checklist checklist);

    @Nullable
    public Checklist read(long id);

    public void update(Checklist checklist);

    public void delete(long id);

    @NonNull
    public List<Checklist> getAllChecklists();

    public void saveAllChecklists(List<Checklist> checklists);

}
