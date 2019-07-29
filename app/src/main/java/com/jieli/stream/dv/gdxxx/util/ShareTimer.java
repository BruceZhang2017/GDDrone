package com.jieli.stream.dv.gdxxx.util;

import android.os.SystemClock;
import android.util.Log;

import com.jieli.stream.dv.gdxxx.ui.a;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
//mDeviceTimer=new ShareTimer(iSendTimerInterval,new ShareTimer.Task(){
//@Override
//public void onTimer(int iTimerCount) {
//        //if(mCallBack!=null && t.isFinishing()==false) {
//        //mCallBack.onDeviceSendTimer(iTimerCount);
//        //}
//        //mDeviceConnectTime=ShareTime.getTick();
//        }
//        });

/**
 * Created by QQ667661 on 16/2/27. 建议单例保证防止越来越快
 */
public class ShareTimer {
    public Timer Timer;
    public TimerTask TimerTask;
    public int TimerID;
    public int TimerCount=0;
    private ThreadTask mTimerThreadTask;
    private MainTask mTimerMainTask;
    private long lastTick=0;
    public int IntervalMills =0;//定时执行间隔//在主线程中执行的onTimer过程,注意还需要!isFinishing判断
    public static abstract class MainTask{
        //切记此处运行的不是主线程
        public abstract void onTimer(int iTimerCount);
    }
    //在子线程中执行的onTimer过程
    public static abstract class ThreadTask{
        //切记此处运行的不是主线程
        public abstract void onTimer(int iTimerCount);
    }
    private static ArrayList<Integer> mTimerIDs=new ArrayList<Integer>();
    //切记此处运行的不是主线程,建议TimerID在a中建立
    public ShareTimer(int iTimerID, int iIntervalMills, ThreadTask aTimerThreadTask){
        TimerID=iTimerID;
        synchronized (mTimerIDs) {
            for (int i = 0; i < mTimerIDs.size(); i++) {
                if (iTimerID == mTimerIDs.get(i)) {
                    Log.e("ShareTimer", "$$$开始创建子线程定时器(" + IntervalMills + "毫秒),此ThreadTask_onTimer已被重复创建,需建立cancel("+TimerID+")销毁!");
                    return;
                }
            }
            mTimerIDs.add(iTimerID);
        }
        Log.e("ShareTimer", "开始创建子线程定时器("+ IntervalMills +"毫秒),此onTimer在子线程执行不在主线程");
        IntervalMills =iIntervalMills;
        mTimerThreadTask=aTimerThreadTask;
        TimerCount=0;
        //切记此处运行的不是主线程
        TimerTask=new TimerTask() {
            @Override
            public void run() {
                //防止其它问题导致的Timer太快
                long lNow=SystemClock.uptimeMillis();//
                if(lNow-lastTick<IntervalMills){
                    return;
                }
                lastTick=lNow;
                TimerCount++;
                mTimerThreadTask.onTimer(TimerCount);
            }
        };
        Timer = new Timer();
        Timer.schedule(TimerTask, 0, IntervalMills);// 0秒后启动任务,以后每隔1秒执行一次线程 低于1秒会出错?
        //TimerTask.run();
    }
    //切记此处运行的不是主线程,建议TimerID在a中建立
    public ShareTimer(int iTimerID, int iIntervalMills, MainTask aTimerMainTask){
        TimerID=iTimerID;
        synchronized (mTimerIDs) {
            for (int i = 0; i < mTimerIDs.size(); i++) {
                if (iTimerID == mTimerIDs.get(i)) {
                    Log.e("ShareTimer", "$$$$开始创建子线程定时器(" + IntervalMills + "毫秒),此MainTask_onTimer已被重复创建,需建立cancel("+TimerID+")销毁!");
                    return;
                }
            }
            mTimerIDs.add(iTimerID);
        }
        Log.e("ShareTimer", "开始主线程定时器("+ IntervalMills +"毫秒),此onTimer在主线程执行");
        IntervalMills =iIntervalMills;
        mTimerMainTask=aTimerMainTask;
        TimerCount=0;
        //切记此处运行是主线程
        TimerTask=new TimerTask() {
            @Override
            public void run() {
                //防止其它问题导致的Timer太快
                long lNow=SystemClock.uptimeMillis();//ShareTime.getTick();
                if(lNow-lastTick<IntervalMills){
                    return;
                }
                lastTick=lNow;
                a.MainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        TimerCount++;
                        if(TimerCount%10==0) {
                            Log.e("ShareTimer_MainTask", ":" + TimerCount);
                        }
                        mTimerMainTask.onTimer(TimerCount);
                    }
                });
            }
        };
        Timer = new Timer();
        Timer.schedule(TimerTask, 0, IntervalMills);// 0秒后启动任务,以后每隔1秒执行一次线程 低于1秒会出错?
        //TimerTask.run();
    }
    public void cancel(){
        invalidate();
    }
    //停止Timer
    public void invalidate(){
        TimerCount=0;
        //Timer销毁注意,必须先销毁Timer.cancel才能TimerTask.cancel,否则销毁不了会引起重复在运行Timer后面Timer变快
        if(Timer!=null){
            Log.e("ShareTimer", "停止定时器("+ IntervalMills +")");
            Timer.cancel();
            Timer=null;
        }
        if(TimerTask!=null){
            TimerTask.cancel();
            TimerTask=null;
        }
        if(TimerTask!=null) {
            TimerTask = null;
        }
        //isResume
        synchronized (mTimerIDs) {
            for (int i = mTimerIDs.size()-1; i >-1; i--) {
                if (TimerID == mTimerIDs.get(i)) {
                    mTimerIDs.remove(i);
                    return;
                }
            }
        }
        //不然有可能越来越快,没有销毁
        System.gc();
    }
    public static long getTick(){
        return SystemClock.uptimeMillis();
    }
}


