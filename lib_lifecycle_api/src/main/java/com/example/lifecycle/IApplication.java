package com.example.lifecycle;

import android.content.Context;

public interface IApplication {
    int MIN = 1;
    int NORMAL = 50;
    int MAX = 100;

    /**
     * 组件初始化执行
     */
    void onCreate(Context context);

    /**
     * 组件销毁执行
     */
    void onTerminate();

    /**
     * 组件执行生命周期方法的优先级, [1, 100]之间, 可以使用
     * {@link #MIN}, {@link #NORMAL}, {@link #MAX}
     */
    int priority();
}
