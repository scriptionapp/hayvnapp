package com.hayvn.hayvnapp.Interfaces;

import com.hayvn.hayvnapp.Model.ObjectContainer;

public interface CallbackObjectUpdated {
    void onObjectUpdatedLocally(ObjectContainer obj);
    void onObjectGotFireId(ObjectContainer obj, boolean internet_was_avail);
}

