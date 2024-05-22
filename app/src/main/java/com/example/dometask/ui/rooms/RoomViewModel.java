package com.example.dometask.ui.rooms;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RoomViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public RoomViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is room fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}