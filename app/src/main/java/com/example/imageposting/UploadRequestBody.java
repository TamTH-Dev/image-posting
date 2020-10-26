package com.example.imageposting;

import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
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
    private final int DEFAULT_BUFFER_SIZE = 2048;

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
    public void writeTo(BufferedSink sink) throws IOException {
        long length = this.contentLength();
        byte[] buffer = new byte[this.DEFAULT_BUFFER_SIZE];
        FileInputStream fileInputStream = new FileInputStream(this.file);
        long uploaded = 0;
        Handler handler = new Handler(Looper.getMainLooper());

        int data;
        while ((data = fileInputStream.read(buffer)) != -1) {
            handler.post(new ProgressUpdater(uploaded,length));
            uploaded += data;
            sink.write(buffer, 0, data);
        }
    }

    private class ProgressUpdater implements Runnable {
        private long uploaded;
        private long total;

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
