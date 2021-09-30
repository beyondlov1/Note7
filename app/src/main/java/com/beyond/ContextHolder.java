package com.beyond;


import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * @author: beyond
 * @date: 2021/9/29
 */

public class ContextHolder {
    public static Reference<ListFragment> listFragment;

    public static ListFragment getCurrentListFragment(){
        if (listFragment == null){
            return null;
        }
        return listFragment.get();
    }

    public static void setCurrentListFragment(ListFragment listFragment){
        ContextHolder.listFragment = new WeakReference<>(listFragment);
    }
}
