package com.example.common;

/**
 * 常量类
 */
public class Const {

    /**
     * key的常量,应用于带有键值对中的key.
     * <pre>
     *     code:200
     * </pre>
     */
    final public static class Key {
        public static final String CODE = "code";
        public static final String MESSAGE = "message";
    }

    /**
     * 响应相关常量
     */
    final public static class Resp {
        /**
         * 响应成功
         */
        public static final int SUCCESS = 200;

        /**
         * 参数错误
         */
        public static final int PARAM_ERROR = 300;

    }


    /**
     * 请求码
     */
    final public static class Request {
        public static final int MUSIC = 1;
        public static final int VIDEO = 2;
        public static final int LOGIN = 3;
    }
}
