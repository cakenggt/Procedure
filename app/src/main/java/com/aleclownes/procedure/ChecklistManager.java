package com.aleclownes.procedure;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.List;

/**
 * Created by alownes on 10/29/2015.
 */
public interface ChecklistManager {

    public static final String CHECKLIST_LIST_FILENAME = "checklists.bin";

    public long create(Checklist checklist);

    public Checklist read(long id);

    public void update(Checklist checklist);

    public void delete(long id);

    public List<Checklist> getAllChecklists();

}
