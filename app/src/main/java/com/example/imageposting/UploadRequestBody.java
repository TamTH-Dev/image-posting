package com.example.imageposting;

import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class UploadRequestBody extends RequestBody {
    private final File file;
    private final String contentType;
    private final UploadCallback callback;

    public UploadRequestBody(File file, String contentType, UploadCallback callback) {
        this.file = file;
        this.contentType = contentType;
        this.callback = callback;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType + "/*");
    }

    @Override
    public long contentLength() {
        return this.file.length();
    }

    @Override
    public void writeTo(@NotNull BufferedSink sink) {
        byte[] buffer = new byte[2048];
        try {
            FileInputStream fileInputStream = new FileInputStream(this.file);
            Handler handler = new Handler(Looper.getMainLooper());

            int data;
            long uploaded = 0;

            while ((data = fileInputStream.read(buffer)) != -1) {
                handler.post(new ProgressUpdater(uploaded, this.contentLength()));
                uploaded += data;
                sink.write(buffer, 0, data);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private class ProgressUpdater implements Runnable {
        private final long uploaded;
        private final long total;

        public ProgressUpdater(long uploaded, long total) {
            this.uploaded = uploaded;
            this.total = total;
        }

        @Override
        public void run() {
            callback.onProgressUpdate((int) (100 * this.uploaded / this.total));
        }
    }
}
