package org.nanomodeller.Tools;

public class StringUtils {

    public static boolean isEmpty(String text){
        return text == null || text.length() == 0;
    }
    public static boolean isNotEmpty(String text){
        return !isEmpty(text);
    }
    public static String nvl (String text, String whatIfEmpty){
        if (isEmpty(text)){
            return whatIfEmpty;
        }
        return  text;
    }
    public static String toDoubleQuotes(String text){
        return "\""+text+"\"";
    }

    public static String toSingleQuotes(String text){
        return "\'"+text+"\'";
    }

    public static Object nvl (Object object, Object whatIfEmpty){
        if (object == null){
            return whatIfEmpty;
        }
        return  object;
    }
    public static boolean equals(String first, String second){
        if (first == null && second == null){
            return true;
        }
        else if (first == null || second == null){
            return false;
        }
        else {
            return first.equals(second);
        }
    }
    public static boolean equalsIgnoreNullWhitespace(String first, String second){
        if (isEmpty(first) && isEmpty(second)){
            return true;
        }
        else if (isEmpty(first) || isEmpty(second)){
            return false;
        }
        else {
            return first.equals(second);
        }
    }

}
