package com.iconbet.score.authorization.utils;

import score.ArrayDB;


public final class ArrayDBUtils {
    public static void clearArrayDb(ArrayDB<?> array_db) {
        int size = array_db.size();
        for (int i = 0; i < size - 1; i++) {
            array_db.pop();
        }

    }

    public static <T> boolean removeArrayItem(ArrayDB<T> array_db, Object target) {
        int size = array_db.size();
        T _out = array_db.get(size - 1);
        if (_out.equals(target)) {
            array_db.pop();
            return false;
        }
        for (int i = 0; i < size - 1; i++) {
            if (array_db.get(i).equals(target)) {
                array_db.set(i, _out);
                array_db.pop();
                return false;
            }
        }
        return false;
    }


    public static <T> boolean containsInArrayDb(T value, ArrayDB<T> array) {
        boolean contains = false;
        if (array == null || value == null) {
            return contains;
        }

        for (int i = 0; i < array.size(); i++) {
            if (array.get(i) != null && array.get(i).equals(value)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public static <T> boolean containsInArray(T value, T[] array){
        boolean contains = false;
        if (array == null || value == null){
            return contains;
        }

        for (int i = 0; i < array.length; i++){
            if (array[i] != null && array[i].equals(value)){
                contains = true;
                break;
            }
        }
        return contains;
    }
}