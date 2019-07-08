package org.flussig.documentation.util;

/**
 * Another implementation of elementary string checks - done to avoid external dependencies
 * */
public class Strings {
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotNullOrEmpty(String s) {
        return !isNullOrEmpty(s);
    }
}
