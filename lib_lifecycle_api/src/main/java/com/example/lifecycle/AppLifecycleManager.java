package com.example.lifecycle;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AppLifecycleManager {

    private static final ArrayList<IApplication> iApps = new ArrayList<>();

    /*
    // 通过asm操作字节码将生成如下的静态代码块
    static {
        register("a.b.c.IApplicationImpl");
        register("a.b.d.IApplicationImpl");
        init();
    }
    */

    public static void onCreate(Context context) {
        for (IApplication app : iApps) {
            app.onCreate(context);
        }
    }

    public static void onTerminate() {
        for (IApplication app : iApps) {
            app.onTerminate();
        }
    }

    // asm操作字节码调用
    private static void register(String classname){
        try {
            iApps.add((IApplication) Class.forName(classname).newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // asm操作字节码调用
    private static void init() {
        Collections.sort(iApps, new Comparator<IApplication>() {
            @Override
            public int compare(IApplication o1, IApplication o2) {
                // 按优先级降序
                return o2.priority() - o1.priority();
            }
        });
    }
}
