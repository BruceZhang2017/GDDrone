package com.jieli.stream.dv.gdxxx.ui.widget.pullrefreshview.support.utils;


public class Utils {

    public static final boolean isClassExists(String classFullName) {
        try {
            Class.forName(classFullName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
