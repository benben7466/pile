package com.common.tools;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.TextView;

import com.common.tools.VLog;
import com.demo.my.androiddemo.R;

/**
 * Created by benzhiqiang on 2023/8/2.
 */

public class StringUtil {


    public static String intToStr(int i) {
        return i + "";
    }

    public static String floatToStr(float i) {
        return i + "";
    }

    //字符串转成int
    public static int str2Int(final String str) {
        int result = 0;
        if (isEmptyOrNull(str)) {
            return result;
        }
        try {
            result = Integer.parseInt(str.trim());
        } catch (NumberFormatException e1) {
        }

        return result;
    }

    //string转成float
    public static float stringToFloat(String data) {
        float result = 0.0f;
        if (isEmptyOrNull(data)) {
            return result;
        }

        try {
            result = Float.parseFloat(data);
        } catch (NumberFormatException e) {

        }
        return result;
    }

    //判断字符串是否为null 或 空
    public static boolean isEmptyOrNull(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }

        if (str.equalsIgnoreCase("null")) {
            return true;
        }
        return false;
    }


}
