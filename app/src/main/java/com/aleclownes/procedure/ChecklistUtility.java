package com.aleclownes.procedure;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by alownes on 11/10/2015.
 */
public abstract class ChecklistUtility {

    public static String MODE_KEY = "MODE";
    public static final String ID_KEY = "com.aleclownes.procedure.id";
    public final static String CHECKLIST_TYPE_KEY = "com.aleclownes.procedure.mode";
    public static final String EDIT_MODE = "EDIT";
    public static final String CHECK_MODE = "CHECK";

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View focus = activity.getCurrentFocus();
        if (focus != null) {
            inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
    }

}
