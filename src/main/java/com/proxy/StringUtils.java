package com.proxy;

import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * Created by jerry on 2016/8/23.
 */
public class StringUtils {
    public static boolean isEmpty(String str){
        return str == null || str.length()==0;
    }
    public static boolean isEmpty(CharSequence charSequence){
        return charSequence == null || isEmpty(charSequence.toString());
    }
}
