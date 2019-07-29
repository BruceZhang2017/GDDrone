package com.jieli.stream.dv.gdxxx.util;

import com.jieli.lib.dv.control.player.VideoThumbnail;

/**
 * Description:
 * Author:created by bob on 17-9-4.
 */
public class ThumbnailManager {
    private static VideoThumbnail instance;

    public static VideoThumbnail getInstance() {

        if (instance == null) {
            synchronized (ThumbnailManager.class) {
                if (instance == null) {
                    instance = new VideoThumbnail();
                }
            }
        }
        return instance;
    }

    public static void release() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }
}
