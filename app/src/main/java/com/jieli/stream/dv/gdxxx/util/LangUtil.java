package com.jieli.stream.dv.gdxxx.util;

import java.util.Locale;

/**
 * Description:
 * Author:created by bob on 18-2-21.
 */
public class LangUtil {
    private static String tag = "LangUtil";
    /**
     * 语言设置
     */
    public static final Locale[] LOCALES ={Locale.SIMPLIFIED_CHINESE, Locale.CHINESE, Locale.JAPANESE, Locale.KOREAN, Locale.US, Locale.FRENCH};

    //设备当前为简体中文
    private static final String ARGS_LANG_ZH_CN = "1";
    //设备当前为繁体中文
    private static final String ARGS_LANG_ZH_TW = "2";
    //设备当前为日语
    private static final String ARGS_LANG_JA_JP = "3";
    //韩语
    private static final String ARGS_LANG_KO_KR = "4";
    //设备当前为英文
    private static final String ARGS_LANG_EN_US = "5";
    //法语
    private static final String ARGS_LANG_FR_LU = "6";
    //设备当前为德语
    private static final String ARGS_LANG_DE_DE = "7";
    //设备当前为西班牙语
    private static final String ARGS_LANG_ES_ES = "0";
    ///意大利语
    private static final String ARGS_LANG_IT_IT = "8";
    //荷兰语
    private static final String ARGS_LANG_NL_NL = "9";
    //葡萄牙语
    private static final String ARGS_LANG_PT_PT = "10";
    //瑞典语
    private static final String ARGS_LANG_SV_SE = "11";
    //捷克语
    private static final String ARGS_LANG_CS_CZ = "12";
    //丹麦语
    private static final String ARGS_LANG_DA_DK = "13";
    //波兰语
    private static final String ARGS_LANG_PL_PL = "14";
    //俄语
    private static final String ARGS_LANG_RU_RU = "15";
    //土耳其语
    private static final String ARGS_LANG_TR_TR = "16";
    //希伯来语
    private static final String ARGS_LANG_HE_IL = "17";
    //泰语
    private static final String ARGS_LANG_TH_TH = "18";
    //匈牙利语
    private static final String ARGS_LANG_HU_HU = "19";
    //罗马尼亚语
    private static final String ARGS_LANG_RO_RO = "20";
    //阿拉伯语
    private static final String ARGS_LANG_AR_AR = "21";

    public static Locale getLanguage(String languageCode){
        Locale locale = null;
        switch (languageCode) {
            case ARGS_LANG_ZH_CN:
                locale= (Locale.SIMPLIFIED_CHINESE);
                break;
            case ARGS_LANG_ZH_TW:
                locale=(Locale.TRADITIONAL_CHINESE);
                break;
            case ARGS_LANG_EN_US:
                locale=(Locale.US);
                break;
            case ARGS_LANG_DE_DE:
                locale=(Locale.GERMANY);
                break;
            case ARGS_LANG_JA_JP:
                locale=(Locale.JAPAN);
                break;
            case ARGS_LANG_ES_ES:
                locale = new Locale("es", "ES");
                break;
            case ARGS_LANG_KO_KR:
                locale = new Locale("ko", "KR");
                break;
            case ARGS_LANG_FR_LU://7
                locale = new Locale("fr", "LU");
                break;
            case ARGS_LANG_IT_IT://8
                locale = new Locale("it", "IT");
                break;
            case ARGS_LANG_NL_NL://9
                locale = new Locale("nl", "KR");
                break;
            case ARGS_LANG_PT_PT://10
                locale = new Locale("pt", "PT");
                break;
            case ARGS_LANG_SV_SE://11
                locale = new Locale("sv", "SE");
                break;
            case ARGS_LANG_CS_CZ://12
                locale = new Locale("cs", "CZ");
                break;
            case ARGS_LANG_DA_DK://13
                locale = new Locale("da", "DK");
                break;
            case ARGS_LANG_PL_PL://14
                locale = new Locale("pl", "PL");
                break;
            case ARGS_LANG_RU_RU://15
                locale = new Locale("ru", "RU");
                break;
            case ARGS_LANG_TR_TR://16
                locale = new Locale("tr", "TR");
                break;
            case ARGS_LANG_HE_IL://17
                locale = new Locale("he", "IL");
                break;
            case ARGS_LANG_TH_TH://18
                locale = new Locale("th", "TH");
                break;
            case ARGS_LANG_HU_HU://19
                locale = new Locale("hu", "HU");
                break;
            case ARGS_LANG_RO_RO://20
                locale = new Locale("ro", "RO");
                break;
            case ARGS_LANG_AR_AR://21
                locale = new Locale("ar", "AR");
                break;
            default:
                locale= (Locale.SIMPLIFIED_CHINESE);
                Dbug.e(tag, "Unknown language code" + languageCode);
                break;
        }
        return locale;
    }
}
