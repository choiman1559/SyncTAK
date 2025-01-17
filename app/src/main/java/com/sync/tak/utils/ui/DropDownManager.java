package com.sync.tak.utils.ui;

import android.util.Log;

import com.sync.tak.receivers.CoTUtilityDropDownReceiver;

import java.util.List;
import java.util.Stack;

public class DropDownManager {
    private static final String TAG = DropDownManager.class.getSimpleName();
    private static DropDownManager dropDownManager;
    private final List<String> backstack;

    public static synchronized DropDownManager getInstance() {
        if (dropDownManager == null) {
            dropDownManager = new DropDownManager();
        }
        return dropDownManager;
    }

    public DropDownManager() {
        backstack = new Stack<>();
    }

    /**
     * Pushes an intent action to display a receiver onto the back stack
     * @param action the action to display this receiver
     */
    public void addToBackStack(String action) {
        backstack.add(action);
    }

    /**
     * Removes an intent action to display a receiver from the back stack
     */
    public void removeFromBackStack() {
        if (!backstack.isEmpty()) {
            backstack.remove(backstack.size() - 1);
        } else {
            Log.w(TAG, "Back stack is empty");
        }
    }

    /**
     * Removes all stored entries from the back stack
     */
    public void clearBackStack() {
        backstack.clear();
    }

    public boolean isEmpty() {
        return backstack.isEmpty();
    }

    public int size() {
        return backstack.size();
    }

    public String top() {
        if (!backstack.isEmpty()) {
            return backstack.get(backstack.size()-1);
        } else {
            Log.w(TAG, "Back stack is empty");
            return CoTUtilityDropDownReceiver.SHOW_PLUGIN;
        }
    }
}
