package ru.dimasokol.school.shareqr.model;

import androidx.lifecycle.LiveData;

public class QrLiveData extends LiveData<QrData> {

    @Override
    protected void postValue(QrData value) {
        super.postValue(value);
    }
}
