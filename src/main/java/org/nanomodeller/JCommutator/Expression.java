package org.nanomodeller.JCommutator;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Expression {

    // region application's logic

    public void sortAll() {

        int mapSizeAfterSort = -1;
        int mapSizeBeforeSort = delta.size();

        while (mapSizeAfterSort != mapSizeBeforeSort) {
            Hashtable<String, ArrayList<String>> mapCopy = (Hashtable<String, ArrayList<String>>) delta.clone();
            mapSizeBeforeSort = mapCopy.size();
            for (String s : mapCopy.keySet()) {

                String previousValue = null;
                String sortedValue = "";
                while (!sortedValue.equals(previousValue)) {
                    for (int i = 0; i < mapCopy.get(s).size(); i++){
                        previousValue = delta.get(s).get(i);
                        sort(s);
                        sortedValue = delta.get(s).get(i);;
                    }
                }
            }
            mapSizeAfterSort = delta.size();
        }
    }

    public void sort(String part) {

        String res = "";
        ArrayList<String> clone = (ArrayList<String>)delta.get(part).clone();
        String sortedVal = "";
        for (int iter = 0; iter < clone.size(); iter++) {

            String val = clone.get(iter);
            for (int i = 1; i < val.length() - 1; i++) {
                for (int j = 1; j < val.length() - 1; j++) {
                    if (isGreater(val.charAt(j), val.charAt(j + 1))) {
                        val = swap(j, j + 1, part, val);
                        if ("0".equals(val)) {
                            delta.get(part).remove(iter);
                            return;
                        }
                    }
                }
            }
            delta.get(part).remove(iter);
            delta.get(part).add(iter, val);
        }
    }

    public void simplifyAll() {
        for (String s : delta.keySet()) {
            simplify(delta.get(s));
        }
    }


    public String ArrayListToString(ArrayList<String> list){
        String result = "";
        for (String s : list){
            result += s;
        }
        return result;
    }
    public void simplify(ArrayList<String> list){
        simplify(list, true);
    }
    public void simplify(ArrayList<String> list, boolean findCommon) {

        String prevValue = "";
        String newValue = " ";
        while (!prevValue.equals(newValue)) {

            prevValue = ArrayListToString(list);
            ArrayList<String> copy = (ArrayList<String>)list.clone();
            for (String s : copy){
                shorten(list, s);
            }
            newValue = ArrayListToString(list);

        }
        newValue = " ";
        prevValue = "";
        while (findCommon && !prevValue.equals(newValue)) {
            prevValue = ArrayListToString(list);
            findCommon(list, false);
            newValue = ArrayListToString(list);
        }
        newValue = " ";
        prevValue = "";
        while (findCommon && !prevValue.equals(newValue)) {
            prevValue = ArrayListToString(list);
            findCommon(list, true);
            newValue = ArrayListToString(list);
        }
        delta.size();
    }


    private String shortenAll(ArrayList<String> list) {

        ArrayList<String> copy = (ArrayList<String>) list.clone();
        for (String s : copy){
            shorten(list, s);
        }
        return ArrayListToString(list);
    }

    private void shorten(ArrayList<String> list, String s) {
        if(list.contains(changeSign(s))){
            list.remove(s);
            list.remove(changeSign(s));
            return;
        }
        if (s.length() == 3 && !antiCommutes(s.charAt(1), s.charAt(2))){
            if (list.contains("-1") && s.charAt(0) == '+'){
                list.remove(s);
                list.remove("-1");
                list.add(changeSign(swapCase(s)));
            }
            else if (list.contains("+1") && s.charAt(0) == '-'){
                list.remove(s);
                list.remove("+1");
                list.add(changeSign(swapCase(s)));
            }
            else if (list.contains("+1") && s.charAt(0) == '0'){
                list.remove(s);
                list.remove("+1");
                list.add(changeSign(swapCase(s)));
            }
            else if(list.contains(swapCase(s))){
                list.remove(s);
                list.remove(swapCase(s));
                if(s.charAt(0) == '+'){
                    list.add("+1");
                }
                else {
                    list.add("-1");
                }
                return;
            }
        }
    }

//    public void findCommon(ArrayList<String> list, boolean forward) {
//        findCommon(list, forward, 0);
//    }

    public void findCommon(ArrayList<String> list, boolean forward){

        if (list.size() <1){
            return;
        }
        char sign = '+';
        String pattern = "";
        String lastMatchingPattern = "";
        for ( String s : list){
            int len = s.length();
            lastMatchingPattern = findPattern(list, forward, s, len, pattern, lastMatchingPattern);
            if(lastMatchingPattern.isEmpty() )
            {
                //findCommon(list, forward, element + 1);
                continue;

            }
            else{
                sign = s.charAt(0);
                break;
            }
        }
        if (lastMatchingPattern.isEmpty()){
            return;
        }
        ArrayList<Integer> matchingItems = new ArrayList<Integer>();
        for (String str : list){
            if (str.contains(lastMatchingPattern)){
                matchingItems.add(list.indexOf(str));
            }
        }
        String newItem = "";
        ArrayList<String> itemsToShorten = new ArrayList<String>();
        ArrayList<String> copy = (ArrayList<String>)list.clone();
        for (int k = 0; k < matchingItems.size(); k++){
            String replacment = "";
//            if (forward){
                replacment = copy.get(matchingItems.get(k)).replace(lastMatchingPattern, "");
//            }
//            else {
//                replacment = replaceLast(copy.get(matchingItems.get(k)), lastMatchingPattern, "");
//            }

            replacment = replacment.length() > 1 ? replacment : replacment + "1";
            itemsToShorten.add(replacment);
            String toRemove = copy.get(matchingItems.get(k));
            list.remove(toRemove);
        }
        shortenAll(itemsToShorten);
        findCommon(itemsToShorten, !forward);
        newItem = ArrayListToString(itemsToShorten);
        ArrayList<String> al = StringToArrayList(lastMatchingPattern);
        if (al.size() > 1){
            simplify(al);
            if (al.size() >1){
                lastMatchingPattern =  "(" + ArrayListToString(al) +")";
            }else {
                lastMatchingPattern = ArrayListToString(al);
            }
        }

        if (!newItem.isEmpty()) {
            if (itemsToShorten.size() > 1) {
                if (forward) {
                    if (sign == newItem.charAt(0)) {
                        list.add("+" + lastMatchingPattern + "(" + newItem + ")");
                    } else {
                        list.add("-" + lastMatchingPattern + "(" + newItem + ")");
                    }
                } else {
                    if (sign == newItem.charAt(0)) {
                        list.add("+" + "(" + newItem + ")" + lastMatchingPattern);
                    } else {
                        list.add("-" + "(" + newItem + ")" + lastMatchingPattern);
                    }

                }
            }
            else{
                if (forward) {
                    if (sign == newItem.charAt(0)) {
                        list.add("+" + lastMatchingPattern  + newItem.substring(1));
                    } else {
                        list.add("-" + lastMatchingPattern  + newItem.substring(1));
                    }
                } else {
                    if (sign == newItem.charAt(0)) {
                        list.add("+" + newItem.substring(1) + lastMatchingPattern);
                    } else {
                        list.add("-" + newItem.substring(1) + lastMatchingPattern);
                    }

                }
            }
        }
    }

    public static String replaceLast(String text, String toReplace, String replacement) {
        int pos = text.lastIndexOf(toReplace);
        if (pos > -1) {
            return text.substring(0, pos)
                    + replacement
                    + text.substring(pos + toReplace.length(), text.length());
        } else {
            return text;
        }
    }

    public ArrayList<String> StringToArrayList(String s){
        ArrayList<String> result = new ArrayList<>();
        if (s.length() > 0) {
            String temp = s;
            if (s.charAt(0) == '(' && s.charAt(s.length()-1) == ')'){
                temp = s.substring(1, s.length()-1 );
            }
            String res = "";
            for (int i = 0; i < temp.length(); i++) {
                if (!res.isEmpty() && countOccurences(res, "(") == countOccurences(res,")") && (temp.charAt(i) == '+' || temp.charAt(i) == '-')) {
                    result.add(res);
                    res = "" + temp.charAt(i);
                }
                else{
                    res += "" + temp.charAt(i);
                }

            }
            result.add(res);
        }
        return result;
    }

    public int countOccurences(String s, String pattern) {
        int counter = 0;
        if (s.indexOf(pattern) > -1){
            for (int index = s.indexOf(pattern); index <= s.lastIndexOf(pattern); index = s.indexOf(pattern, index + 1)) {
                counter++;
            }
        }
        return counter;
    }

    private String findPattern(ArrayList<String> list, boolean forward, String s, int len, String pattern, String lastMatchingPattern) {
        for (int i = 0; i <= len; i++) {
            boolean hasPattern = false;
            for (String str : list) {
                if (!s.equals(str)) {
                    if (str.contains(pattern)) {
                        hasPattern |= true;
                        break;
                    }
                }
            }
            if (hasPattern) {
                lastMatchingPattern = pattern;
                if (forward) {
                    if (i > 0) {
                        pattern = s.substring(1, i);
                        pattern = pattern + "";
                    }

                } else {
                    pattern = s.substring(len - i, len);
                    pattern = pattern + "";
                }

            }
        }

        return lastMatchingPattern;
    }

    public String swap(int i, int j, String key, String value) {


        String result = "";
        if (!(i - j == 1 || i - j == -1)) {
            return value;
        }
        if (i != j && value.charAt(i) == value.charAt(j)){
            return "0";
        }
        int first = i < j ? i : j;
        int second = i >= j ? i : j;
        String firstPart = value.substring(0, first);
        String secondPart = value.substring(second + 1);
        char firstChar = value.charAt(first);
        char secondChar = value.charAt(second);
        char firstInAlphabeticOrder = Character.toLowerCase(firstChar) < Character.toLowerCase(secondChar) ?
                Character.toLowerCase(firstChar) : Character.toLowerCase(secondChar);
        char secondInAlphabeticOrder = Character.toLowerCase(firstChar) > Character.toLowerCase(secondChar) ?
                Character.toLowerCase(firstChar) : Character.toLowerCase(secondChar);

        if (antiCommutes(firstChar, secondChar)) {
            result = changeSign(swapChar(value, first, second));
        }
        else {
            String newKey = "" + firstInAlphabeticOrder + secondInAlphabeticOrder;
            String newValue = firstPart + secondPart;
            if (newValue.length() == 1) {
                newValue += "1";
            }
            if (!key.equals(noDelta) && !key.equals(newKey)) {
                if(key.contains(newKey))
                {
                    newKey = key;
                }
                else {
                    newKey += key;
                }
            }
            if (deltaEqualsOne(newKey)){
                newKey = noDelta;
            }
            if (!delta.containsKey(newKey)) {
                delta.put(newKey, new ArrayList<>());
                delta.get(newKey).add(newValue);
            } else {
                delta.get(newKey).add(newValue);
            }
            result = changeSign(swapChar(value, first, second));
        }
        return result;
    }

    public boolean deltaEqualsOne(String key){
        if (key.isEmpty()){
            return true;
        }
        char temp = key.charAt(0);
        int len = key.length();
        for (int i = 0; i < len; i++){
            if (key.charAt(i) != temp){
                return false;
            }
        }
        return true;
    }

    public String add(String s1, String s2) {

        String result = "";
        if (s1.length() > 0 && s2.length() > 0){
            for (int index = s1.indexOf(s2.substring(1)); index >= 0; index = s1.indexOf(s2.substring(1), index + 1)) {
                try {
                    if ((s1.charAt(index - 1) == '+' || s1.charAt(index - 1) == '-') && s1.charAt(index - 1) != s2.charAt(0)
                            &&
                            (s1.charAt(index + s2.length()) == '-' || s1.charAt(index + s2.length()) == '+')) {
                        result = s1.substring(0, index - 1) + s1.substring(index + s2.length());
                        return result;
                    }
                } catch (IndexOutOfBoundsException e) {
                    if ((s1.charAt(index - 1) == '+' || s1.charAt(index - 1) == '-') && s1.charAt(index - 1) != s2.charAt(0)) {
                        result = s1.substring(0, index - 1);
                        return result;
                    }

                }
            }
        }
        if (s1.length() > 0 && s2.length() > 0 && s1.charAt(0) != s2.charAt(0) && s1.substring(1).equals(s2.substring(1))) {
            return "";
        } else {
            return s1 + s2;
        }
    }

    public Expression add(Expression e) {
        Expression result;
        result = new Expression();
        for (String key : delta.keySet()) {
            if (e.delta.containsKey(key)) {
                result.delta.put(key, new ArrayList<String>());
                result.delta.get(key).addAll(delta.get(key));
                result.delta.get(key).addAll(e.delta.get(key));
            } else {
                result.delta.put(key, delta.get(key));
            }
        }
        for (String key : e.delta.keySet()) {
            if (!delta.containsKey(key)) {
                result.delta.put(key, e.delta.get(key));
            }
        }
        return result;
    }
    // region operators relations

    public boolean antiCommutes(char firstChar, char secondChar) {

        Character[] atomCreationPlus = {AiS, AjS, AlS, AmS};
        Character[] atomCreationMinus = {Ais, Ajs, Als, Ams};
        Character[] atomAnnihilationPlus = {aiS, ajS, alS, amS};
        Character[] atomAnnihilationMinus = {ais, ajs, als, ams};
        Character[] electrodeCreationPlus = {AkS, AqS, ArS, AwS};
        Character[] electrodeCreationMinus = {Aks, Aqs, Ars, Aws};
        Character[] electrodeAnnihilationPlus = {akS, aqS, arS, awS};
        Character[] electrodeAnnihilationMinus = {aks, aqs, ars, aws};

        if (Arrays.asList(atomAnnihilationPlus).contains(firstChar) && Arrays.asList(atomCreationPlus).contains(secondChar)||
                Arrays.asList(atomAnnihilationPlus).contains(secondChar) && Arrays.asList(atomCreationPlus).contains(firstChar)||
                Arrays.asList(atomAnnihilationMinus).contains(firstChar) && Arrays.asList(atomCreationMinus).contains(secondChar)||
                Arrays.asList(atomAnnihilationMinus).contains(secondChar) && Arrays.asList(atomCreationMinus).contains(firstChar)||
                Arrays.asList(electrodeAnnihilationPlus).contains(firstChar) && Arrays.asList(electrodeCreationPlus).contains(secondChar)||
                Arrays.asList(electrodeAnnihilationPlus).contains(secondChar) && Arrays.asList(electrodeCreationPlus).contains(firstChar)||
                Arrays.asList(electrodeAnnihilationMinus).contains(firstChar) && Arrays.asList(electrodeCreationMinus).contains(secondChar)||
                Arrays.asList(electrodeAnnihilationMinus).contains(secondChar) && Arrays.asList(electrodeCreationMinus).contains(firstChar))
        {
            return false;
        }
        return  true;
    }

    public boolean isGreater(char one, char two) {
        if (Character.isLowerCase(one) && Character.isLowerCase(two) || Character.isUpperCase(one) && Character.isUpperCase(two)) {
            return one > two;
        } else if (Character.isUpperCase(one)) {
            if (Character.toUpperCase(two) == one) {
                return true;
            }
            return one > Character.toUpperCase(two);
        }
        return Character.toUpperCase(one) > two;

    }

    // endregion operators relations

    // endregion application's logic

    //region text operations

    public String changeSign(String text) {
        if (text.charAt(0) == '+') {
            return text.replace('+', '-');
        } else {
            return text.replace('-', '+');
        }
    }

    public boolean isOpositeSign(String text1, String text2){
        if(text1.charAt(0) != text2.charAt(0)){
            if(text1.substring(1).equals(text2.substring(1))){
                return true;
            }
        }
        return  false;
    }

    boolean equalIgnoreCase(Character a, Character b){
        return Character.toUpperCase(a) == Character.toUpperCase(b);
    }

    boolean hasDifferentCase(Character a, Character b){
        return Character.isUpperCase(a) && Character.isLowerCase(b) ||
                Character.isUpperCase(b) && Character.isLowerCase(a);
    }

    boolean hasDuplicates(String text){
        char[] array = text.toCharArray();
        Set<Character> set = new HashSet<Character>();
        for (char i : array)
        {
            if (set.contains(i)) return true;
            set.add(i);
        }
        return false;
    }

    public String swapCase(String text){
        String result = "";
        for (int i = 0; i < text.length(); i++){
            if(Character.isUpperCase(text.charAt(i))){
                result += Character.toLowerCase(text.charAt(i));
            }
            else{
                result += Character.toUpperCase(text.charAt(i));
            }
        }
        return result;
    }

    public String changeLetters(String text) {
        String result = "";
        for (int i = 0; i < text.length(); i++) {
            switch (Character.toLowerCase(text.charAt(i))) {
                case AiS:
                    result += "iσ";
                    break;
                case Ais:
                    result += "i-σ";
                    break;
                case AjS:
                    result += "jσ";
                    break;
                case Ajs:
                    result += "j-σ";
                    break;
                case AlS:
                    result += "lσ";
                    break;
                case Als:
                    result += "l-σ";
                    break;
                case AmS:
                    result += "mσ";
                    break;
                case Ams:
                    result += "m-σ";
                    break;
                case AkS:
                    result += "kσ";
                    break;
                case Aks:
                    result += "k-σ";
                    break;
                case AqS:
                    result += "k'σ";
                    break;
                case Aqs:
                    result += "k'-σ";
                    break;
                case ArS:
                    result += "k''σ";
                    break;
                case Ars:
                    result += "k''-σ";
                    break;
                case AwS:
                    result += "k'''σ";
                    break;
                case Aws:
                    result += "k'''-σ";
                    break;
            }
        }
        return result;
    }

    public String swapChar(String text, int first, int second) {

        String temp = text.substring(0, first) + text.charAt(second) + text.substring(first + 1);
        temp = temp.substring(0, second) + text.charAt(first) + temp.substring(second + 1);
        return temp;
    }

    //endregion text operations

    // region display methods

    public static void displayResult(String[] h, String operator) {
        String toDisplay = "<html><head><meta charset=\"UTF-8\"></head>";
        for (int iter = 0; iter < h.length; iter++){
            Commutator c = new Commutator(h[iter], operator);


            int mapSizeAfterSort = -1;
            int mapSizeBeforeSort = 1;



            Expression partialResult = c.evaluate();
            while (mapSizeAfterSort != mapSizeBeforeSort) {
                Hashtable<String, ArrayList<String>> mapCopy = (Hashtable<String, ArrayList<String>>) partialResult.delta.clone();
                mapSizeBeforeSort = mapCopy.size();
                partialResult.sortAll();
//            partialResult.removeZeroElements();
                partialResult.simplifyAll();
                mapSizeAfterSort = partialResult.delta.size();
            }
//            partialResult.sortAll();
////            partialResult.removeZeroElements();
//            partialResult.simplifyAll();
            toDisplay += "<p><b>["+partialResult.textToHTML(h[iter]) +", "+partialResult.textToHTML(operator)+" ]</b> = " + partialResult.hashmapToHTMLString() +"</p>" ;
        }
        toDisplay += "</html>";
        PrintWriter pw = null;
        try{
            pw = new PrintWriter("storage/commutators.html");
            pw.println(toDisplay);
            pw.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }

        File file = new File("storage/commutators.html");
        Desktop desktop = Desktop.getDesktop();
        if(file.exists()) try {
            desktop.open(file);
        } catch (IOException exception) {

        }
    }
    public void removeZeroElements(){
        Hashtable<String, ArrayList<String>> mapCopy = (Hashtable<String, ArrayList<String>>) delta.clone();
        for (String key : mapCopy.keySet()){
            ArrayList<String> list = (ArrayList<String>)mapCopy.get(key).clone();
            for (String text : list){
                if (hasDuplicates(text)){
                    delta.get(key).remove(text);
                }
            }
        }
    }

    public String inserHTMLText(String part, int i) {
        String result = "";
        switch (part.charAt(i)) {
            case AiS:
                result += " a<sup>†</sup><sub>iσ</sub>";
                break;
            case aiS:
                result += " a<sub>iσ</sub>";
                break;
            case Ais:
                result += "a<sup>†</sup><sub>i-σ</sub>";
                break;
            case ais:
                result += " a<sub>i-σ</sub>";
                break;
            case AlS:
                result += " a<sup>†</sup><sub>lσ</sub>";
                break;
            case alS:
                result += " a<sub>lσ</sub>";
                break;
            case Als:
                result += "a<sup>†</sup><sub>l-σ</sub>";
                break;
            case als:
                result += " a<sub>l-σ</sub>";
                break;
            case AmS:
                result += " a<sup>†</sup><sub>mσ</sub>";
                break;
            case amS:
                result += " a<sub>mσ</sub>";
                break;
            case Ams:
                result += "a<sup>†</sup><sub>m-σ</sub>";
                break;
            case ams:
                result += " a<sub>m-σ</sub>";
                break;
            case AjS:
                result += " a<sup>†</sup><sub>jσ</sub>";
                break;
            case ajS:
                result += " a<sub>jσ</sub>";
                break;
            case Ajs:
                result += " a<sup>†</sup><sub>j-σ</sub>";
                break;
            case ajs:
                result += " a<sub>j-σ</sub>";
                break;
            case ArS:
                result += " a<sup>†</sup><sub>k''σ</sub>";
                break;
            case arS:
                result += " a<sub>k''σ</sub>";
                break;
            case Ars:
                result += " a<sup>†</sup><sub>k''-σ</sub>";
                break;
            case ars:
                result += " a<sub>k''-σ</sub>";
                break;
            case AwS:
                result += " a<sup>†</sup><sub>k'''σ</sub>";
                break;
            case awS:
                result += " a<sub>k'''σ</sub>";
                break;
            case Aws:
                result += " a<sup>†</sup><sub>k'''-σ</sub>";
                break;
            case aws:
                result += " a<sub>k'''-σ</sub>";
                break;
            case AkS:
                result += " a<sup>†</sup><sub>kσ</sub>";
                break;
            case akS:
                result += " a<sub>kσ</sub>";
                break;
            case Aks:
                result += " a<sup>†</sup><sub>k-σ</sub>";
                break;
            case aks:
                result += " a<sub>k-σ</sub>";
                break;
            case AqS:
                result += " a<sup>†</sup><sub>k'σ</sub>";
                break;
            case aqS:
                result += " a<sub>k'σ</sub>";
                break;
            case Aqs:
                result += " a<sup>†</sup><sub>k'-σ</sub>";
                break;
            case aqs:
                result += " a<sub>k'-σ</sub>";
                break;
            default:
                result += " " + part.charAt(i) + " ";
                break;
        }
        return result;
    }

    public String hashmapToHTMLString() {
        String result = "";

        for (String key : delta.keySet()) {

            if (delta.get(key).isEmpty()){
                continue;
            }
            if (!key.equals(noDelta)) {
                result += " +";
            }
            for (int k = 0; k < key.length(); k++) {
                if (!key.equals(noDelta) && k % 2 == 0) {
                    result += " δ<sub>" + changeLetters(key.substring(0 + k, 1 + k))
                            + "," + changeLetters(key.substring(1 + k, 2 + k))+ "</sub>";
                }
            }
            if (!key.equals(noDelta)) {
                result += "(";
            }
            for (String s : delta.get(key)){
                result += textToHTML(s);
            }
            if (!key.equals(noDelta)) {
                result += ") ";
            }
        }
        result = result.replaceAll("\\( \\+" , "(");
        result = result.replaceAll("\\(  \\+" , "(");
        return result.length() > 0 && !result.isEmpty() ? result : "0";
    }

    public String textToHTML(String text) {
        String result = "";
        for (int i = 0; i < text.length(); i++) {
            result += inserHTMLText(text, i);

        }
        return result;
    }

    // endregion display methods

    // region constructors

    public Expression(String noDeltaPart) {
        this.delta.put(noDelta, new ArrayList<>());
        this.delta.get(noDelta).add(noDeltaPart);
    }

    public Expression() {
    }

    // endregion constructors

    //region fields

    public Hashtable<String, ArrayList<String>> delta = new Hashtable<String, ArrayList<String>>();

    public static final String noDelta = "nD";

    // region operators

    public static final char AiS = 'a';//  a†iσ
    public static final char aiS = 'A';//  aiσ
    public static final char AjS = 'b';//  a†jσ
    public static final char ajS = 'B';//  ajσ
    public static final char AlS = 'c';//  a†lσ
    public static final char alS = 'C';//  alσ
    public static final char AmS = 'd';//  a†mσ
    public static final char amS = 'D';//  amσ'
    public static final char Ais = 'e';//  a†i-σ
    public static final char ais = 'E';//  ai-σ
    public static final char Ajs = 'f';//  a†j-σ
    public static final char ajs = 'F';//  aj-σ
    public static final char Als = 'g';//  a†l-σ
    public static final char als = 'G';//  al-σ
    public static final char Ams = 'h';//  a†m-σ
    public static final char ams = 'H';//  am-σ'
    public static final char AkS = 'k';//  a†kσ
    public static final char akS = 'K';//  akσ
    public static final char AqS = 'l';//  a†k'σ
    public static final char aqS = 'L';//  ak'σ
    public static final char ArS = 'm';//  a†k''σ
    public static final char arS = 'M';//  ak''σ
    public static final char AwS = 'n';//  a†k'''σ
    public static final char awS = 'N';//  ak'''σ'
    public static final char Aks = 'o';//  a†k-σ
    public static final char aks = 'O';//  ak-σ
    public static final char Aqs = 'p';//  a†k'-σ
    public static final char aqs = 'P';//  ak'-σ
    public static final char Ars = 'r';//  a†k''-σ
    public static final char ars = 'R';//  ak''-σ
    public static final char Aws = 's';//  a†k'''-σ
    public static final char aws = 'S';//  ak'''-σ

    // endregion operators

    // endregion fields

    // region unused stuff
    //    public void computeAllDeltas(){
//        Hashtable<String, String> mapCopy = (Hashtable<String, String>) delta.clone();
//        for (String s : mapCopy.keySet()){
//            computeDelta(s);
//        }
//    }
//    public void computeDelta(String key){
//
//        String text = delta.get(key);
//        char char1 = key.charAt(0);
//        String result = "";
//        for (char c : text.toCharArray()){
//            if (key.contains(Character.toLowerCase(c) + "")){
//                if (Character.isUpperCase(c)){
//                    result += Character.toUpperCase(char1);
//                }
//                else
//                {
//                    result += Character.toLowerCase(char1);
//                }
//            }
//            else{
//                result += c +"";
//            }
//        }
//        String newKey = Character.toString(char1)+Character.toString(char1);
//        if (delta.containsKey(newKey)){
//            String oldValue = delta.get(newKey);
//            delta.put(newKey, oldValue + result);
//        }else{
//            delta.put(newKey, result);
//        }
//    }


//    public void sort3(String part, String operator) {
//
//        int length = operator.length();
//        String value = delta.get(part);
//        ArrayList<String> array = new ArrayList<String>();
//        ArrayList<String> sortedArray = new ArrayList<String>();
//        String temp = "" + value.charAt(0);
//        String res = "";
//        for (int i = 1; i < value.length(); i++) {
//            if (value.charAt(i) == '+' || value.charAt(i) == '-') {
//                array.addAtom(temp);
//                temp = "";
//            }
//            temp += value.charAt(i);
//        }
//        array.addAtom(temp);
//        String sortedVal = "";
//        String text = array.get(0);
//
//        for(int i = 0; i < length; i++){
//            for (int j = 0; j < text.length() - length- 1; j++){
//                int firstIndex = text.length()- length + i - j;
//                int secondIndex = firstIndex - 1;
//                text = swap(secondIndex, firstIndex,part, text);
//            }
//        }
//
//       // String valueToInsert = delta.get(part).replace(value, text);
//        delta.put(part, "");
//    }
    // endregion unused stuff

    public static void run(String[] H , String operator){
        Expression.displayResult(H, operator);
    }


}