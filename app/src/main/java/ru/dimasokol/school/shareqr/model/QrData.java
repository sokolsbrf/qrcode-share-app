package ru.dimasokol.school.shareqr.model;

import android.graphics.Bitmap;

import java.io.File;

public class QrData {

    private final File mFile;
    private final Bitmap mBitmap;

    public QrData(File file, Bitmap bitmap) {
        mFile = file;
        mBitmap = bitmap;
    }

    public File getFile() {
        return mFile;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
