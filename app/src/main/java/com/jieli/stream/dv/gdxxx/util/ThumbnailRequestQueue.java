package com.jieli.stream.dv.gdxxx.util;

import com.jieli.stream.dv.gdxxx.bean.FileInfo;
import com.jieli.stream.dv.gdxxx.bean.RequestFileInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;


/**
 * Created by 陈森华 on 2017/9/1.
 * 功能：用一句话描述
 */

public class ThumbnailRequestQueue {


    private static final int MAX_SIZE = IConstant.PAGE_SIZE;

    private Queue<RequestFileInfo> requestFileInfoQueue = new ArrayBlockingQueue<RequestFileInfo>(MAX_SIZE);

    public void put(FileInfo fileInfo, boolean isContent) {
        requestFileInfoQueue.offer(new RequestFileInfo(fileInfo, isContent));
    }

    public RequestFileInfo poll() {
        return requestFileInfoQueue.poll();
    }

    public RequestFileInfo peek() {
        return requestFileInfoQueue.peek();
    }

    public boolean isFull() {
        return requestFileInfoQueue.size() >= MAX_SIZE;
    }

    public boolean isEmpty() {
        return requestFileInfoQueue.isEmpty();
    }

    public void clear() {
        requestFileInfoQueue.clear();
    }

    /**
     * 提取请求缩略图失败的fileinfo
     *
     * @return
     */
    public List<FileInfo> getFileInfos() {
        List<FileInfo> fileInfos = new ArrayList<>();
        while (requestFileInfoQueue.peek() != null) {
            RequestFileInfo requestFileInfo = requestFileInfoQueue.poll();
            if (!requestFileInfo.isContent()) {
                fileInfos.add(requestFileInfo.getFileInfo());
            }
        }
        return fileInfos;
    }

    public int size() {
        return requestFileInfoQueue.size();
    }


}
