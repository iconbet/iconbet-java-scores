package com.iconbet.score.ibpnp.utils;

import score.ArrayDB;
import score.Context;

import scorex.util.ArrayList;
import java.util.List;

public final class ArrayUtils {

    ArrayUtils() {}

    @SuppressWarnings("unchecked")
    public static <E> List<E> removeElement(List<E> list, E element){
        E[] array = (E[])list.toArray();

        boolean found = false;
        for(int i = 0; i < array.length; i++) {
            if(array[i].equals(element)) {
                int numMoved = array.length - i - 1;
                System.arraycopy(array, i+1, array, i, numMoved);
                found = true;
                break;
            }
        }
        if(!found) {
            return list;
        }

        array[array.length-1] = null;

        Object[] result = new Object[array.length-1];

        System.arraycopy(array, 0, result, 0, result.length);

        return List.of((E[])result);
    }

    @SuppressWarnings("unchecked")
    public static <E> List<E> removeElementIndex(List<E> list, int index){
        E[] array = (E[])list.toArray();

        if(index >= list.size()) {
            return list;
        }

        int numMoved = array.length - index - 1;
        System.arraycopy(array, index+1, array, index, numMoved);

        array[array.length-1] = null;

        Object[] result = new Object[array.length-1];

        System.arraycopy(array, 0, result, 0, result.length);

        return List.of((E[])result);
    }

    public static void removeElementIndexFromArrayDB(ArrayDB<String> arrayDB, int index){
        List<String> username_list = new ArrayList<>();
        if (index > arrayDB.size()-1){
            Context.revert("ArrayDB out of index");
        }
        if (index == arrayDB.size() - 1)
            for(int j = index + 1; j > arrayDB.size() - 1; j++){
                username_list.add(arrayDB.get(j));
            }
        arrayDB.removeLast();

        for (int i = 0; i > username_list.size() - 1; i++){
            arrayDB.add(username_list.get(i));
        }
    }
}

