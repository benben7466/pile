package com.common.tools;

import android.os.Environment;
import android.util.Log;

import com.demo.my.androiddemo.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by benzhiqiang on 2023/7/27.
 */

public class VLog {

    private static String TAG = "chunbo:";
    private static final String LOG_FILE = "chunbo-log.txt";

    //打印Debug信息
    public static void d(String content) {
        Log.d(TAG, content);
    }

    public static void d(String title, String content) {
        Log.d(TAG + title, content);
    }

    //打印普通信息
    public static void i(String title, String responsString) {
        VLog.d("开始处理", title);

        int index = 0;
        int maxLength = 1000;//每行最大长度
        String _printStr;

        while (index < responsString.length()) {
            if (responsString.length() <= index + maxLength) {// java的字符不允许指定超过总的长度end
                _printStr = responsString.substring(index);
            } else {
                _printStr = responsString.substring(index, index + maxLength);
            }

            index += maxLength;

            VLog.d(_printStr.trim());

        }

    }


    public static void logToFile(String content) {

        try {

            File logFile = new File(Environment.getExternalStorageDirectory(), LOG_FILE);
            FileWriter fw = new FileWriter(logFile, true);
            fw.write(content);
            fw.close();

            VLog.d("日志文件路径：", Environment.getExternalStorageDirectory() + "/" + LOG_FILE);

        } catch (IOException e) {
            VLog.d("日志写入文件异常logToFile");
            e.printStackTrace();
        }
    }

    public static void printException(String title, Exception e) {
        VLog.d(title, e.getMessage());

        if (BuildConfig.DEBUG) {
            e.printStackTrace();
        }
    }

    public static void printException(String title, Error e) {
        VLog.d(title, e.getMessage());

        if (BuildConfig.DEBUG) {
            e.printStackTrace();
        }
    }


}
