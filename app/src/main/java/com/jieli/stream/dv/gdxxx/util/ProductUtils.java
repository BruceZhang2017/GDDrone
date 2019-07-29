package com.jieli.stream.dv.gdxxx.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Description:
 * Author:created by bob on 17-9-19.
 */
public class ProductUtils {
    private static final String[] PHONE_MODEL = {"PLK-AL10"};
    private static final List<String> MODEL_LIST = new ArrayList<>(Arrays.asList(PHONE_MODEL));
    public static boolean isExistModel(String model) {
        return MODEL_LIST.contains(model);
    }
}
