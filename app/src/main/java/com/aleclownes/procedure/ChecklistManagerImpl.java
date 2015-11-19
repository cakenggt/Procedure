package com.aleclownes.procedure;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by alownes on 10/29/2015.
 */
public class ChecklistManagerImpl implements ChecklistManager {

    Context context;

    @Override
    public long create(Checklist checklist) {
        List<Checklist> checklists = (List<Checklist>)readObjectFromMemory(CHECKLIST_LIST_FILENAME);
        if (checklists == null){
            checklists = new ArrayList<Checklist>();
        }
        if (checklist.getId() == null){
            long maxId = 0;
            for (Checklist c : checklists){
                if (c.getId() > maxId){
                    maxId = c.getId();
                }
            }
            checklist.setId(maxId+1);
        }
        checklists.add(checklist);
        saveAllChecklists(checklists);
        return checklist.getId();
    }

    @Override
    public Checklist read(long id) {
        for (Checklist checklist : getAllChecklists()){
            if (checklist.getId() == id){
                return checklist;
            }
        }
        return null;
    }

    @Override
    public void update(Checklist checklist) {
        List<Checklist> checklists = (List<Checklist>)readObjectFromMemory(CHECKLIST_LIST_FILENAME);
        if (checklists == null){
            checklists = new ArrayList<Checklist>();
        }
        checklist.setLastModified(new Date());
        for (int i = 0; i < checklists.size(); i++){
            Checklist next = checklists.get(i);
            if (next.getId().equals(checklist.getId())){
                checklists.set(i, checklist);
                saveAllChecklists(checklists);
                return;
            }
        }
        checklists.add(checklist);
        saveAllChecklists(checklists);
    }

    @Override
    public void delete(long id) {
        List<Checklist> checklists = (List<Checklist>)readObjectFromMemory(CHECKLIST_LIST_FILENAME);
        if (checklists == null){
            checklists = new ArrayList<Checklist>();
        }
        for (int i = 0; i < checklists.size(); i++){
            Checklist next = checklists.get(i);
            if (next.getId() == id){
                checklists.remove(i);
                break;
            }
        }
        saveAllChecklists(checklists);
    }

    public ChecklistManagerImpl(Context context){
        this.context = context;
    }

    @Override
    public List<Checklist> getAllChecklists(){
        List<Checklist> checklists =  (List<Checklist>)readObjectFromMemory(CHECKLIST_LIST_FILENAME);
        if (checklists == null){
            checklists = new ArrayList<Checklist>();
        }
        return checklists;
    }

    public void saveAllChecklists(List<Checklist> checklists){
        writeObjectToMemory(CHECKLIST_LIST_FILENAME, checklists);
    }

    private void writeObjectToMemory(String filename, Object object) {
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(object);
            os.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();

        }
        catch (IOException e) {
            e.printStackTrace();

        }

    }

    private Object readObjectFromMemory(String filename) {
        FileInputStream fis;
        Object object = null;
        try {
            fis = context.openFileInput(filename);
            ObjectInputStream is = new ObjectInputStream(fis);
            object = is.readObject();
            is.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();

        }
        catch (StreamCorruptedException e) {
            e.printStackTrace();

        }
        catch (IOException e) {
            e.printStackTrace();

        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();

        }

        return object;

    }

}
