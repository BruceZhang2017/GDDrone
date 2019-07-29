package com.jieli.stream.dv.gdxxx.interfaces;

import com.jieli.media.codec.bean.MediaMeta;

/**
 * @author zqjasonZhong
 *         date : 2017/12/15
 */
public interface OnAviThumbListener {

    void onCompleted(byte[] bytes, MediaMeta mediaMeta);

    void onError(String msg);
}
