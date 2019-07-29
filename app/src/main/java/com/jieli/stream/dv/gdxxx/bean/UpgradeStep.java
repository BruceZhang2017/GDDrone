package com.jieli.stream.dv.gdxxx.bean;

/**
 * 升级步骤状态
 * @author zqjasonZhong
 *         date : 2017/7/3
 */
public class UpgradeStep {

    private int num;               //序号
    private String description;    //描述
    private int state;             //状态  0：未开始  1： 正在执行  2： 已完成
    private boolean isNeedPb;      //是否需要进度条

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isNeedPb() {
        return isNeedPb;
    }

    public void setNeedPb(boolean needPb) {
        isNeedPb = needPb;
    }
}
