package com.iconbet.score.dividend;

import score.Address;
import score.ArrayDB;
import score.Context;
import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;

public class Utils {

    public <T> T callScore(Class<T> t, Address address, String method, Object... params) {
        return Context.call(t, address, method, params);
    }

    public void callScore(Address address, String method, Object... params) {
        Context.call(address, method, params);
    }

    public void callScore(BigInteger amount, Address address, String method, Object... params) {
        Context.call(amount, address, method, params);
    }

    public static <T> boolean containsInArrayDb(T value, ArrayDB<T> arraydb) {
        boolean found = false;
        if (arraydb == null || value == null) {
            return found;
        }

        for (int i = 0; i < arraydb.size(); i++) {
            if (arraydb.get(i) != null
                    && arraydb.get(i).equals(value)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public static <T> List<T> arrayDBtoList(ArrayDB<T> arraydb, T[] list) {

        for (int i = 0; i < arraydb.size(); i++) {
            list[i] = arraydb.get(i);
        }
        return List.of(list);
    }

    public static List<String> splitString(String toBeSplitted){
        List<String> splitList = new ArrayList<>();
        String splitString = "";
        for (int i = 0; i < toBeSplitted.length(); i++){
            char charAtIndex = toBeSplitted.charAt(i);
            if (charAtIndex != ','){
                splitString += String.valueOf(charAtIndex);
            }
            if (charAtIndex == ','){
                splitList.add(splitString);
                splitString = "";
            }
        }
        splitList.add(splitString);
        return splitList;
    }

}
