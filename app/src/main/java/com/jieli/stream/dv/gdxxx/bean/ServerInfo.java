package com.jieli.stream.dv.gdxxx.bean;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  Server 索引文档的json结构
 * @author zqjasonZhong
 *         date : 2017/7/3
 */
public class ServerInfo {

    /*key : app name value : versions*/
    private Map<String, List<Integer>> androidVersionMap;

    /*key : product value : versions*/
    private Map<String, List<String>> firmwareVersionMap;

    public Map<String, List<Integer>> getAndroidVersionMap() {
        return androidVersionMap;
    }

    public void setAndroidVersionMap(Map<String, List<Integer>> androidVersionMap) {
        this.androidVersionMap = androidVersionMap;
    }

    public Map<String, List<String>> getFirmwareVersionMap() {
        return firmwareVersionMap;
    }

    public void setFirmwareVersionMap(Map<String, List<String>> firmwareVersionMap) {
        this.firmwareVersionMap = firmwareVersionMap;
    }

    @Override
    public String toString() {
        String content = "";
        if(androidVersionMap != null){
            Set<String> keySet = androidVersionMap.keySet();
            content = "androidVersionMap :{\n";
            for (String key : keySet){
                content += "\""+key+"\":[ ";
                List<Integer> versions = androidVersionMap.get(key);
                if(versions != null){
                    for (int i = 0 ; i < versions.size(); i++){
                        Integer vv = versions.get(i);
                        if(i == versions.size() -1){
                            content += vv+"],\n";
                        }else{
                            content += vv+", ";
                        }
                    }
                }
            }
        }
        if(firmwareVersionMap != null){
            Set<String> keySet = firmwareVersionMap.keySet();
            content = "firmwareVersionMap :{\n";
            for (String key : keySet){
                content += "\""+key+"\":[ ";
                List<String> versions = firmwareVersionMap.get(key);
                if(versions != null){
                    for (int i = 0 ; i < versions.size(); i++){
                        String d = versions.get(i);
                        if(i == versions.size() -1){
                            content += d+"],\n";
                        }else{
                            content += d+", ";
                        }
                    }
                }
            }
        }
        return content;
    }
}
