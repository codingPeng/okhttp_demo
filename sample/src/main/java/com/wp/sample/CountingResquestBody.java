package com.wp.sample;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by Administrator on 2016/10/10 0006.
 */

public class CountingResquestBody extends RequestBody {

    protected RequestBody delegate;
    private listener listener;
    private countingSink countingSink;

    public CountingResquestBody(RequestBody delegate, listener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public long contentLength() {
        try {
            return delegate.contentLength();
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        countingSink = new countingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);
        delegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    protected final class countingSink extends ForwardingSink {

        private long bytesWriten = 0L;

        public countingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWriten += byteCount;
            listener.onRequestProgress(bytesWriten, contentLength());
        }
    }

    public static interface listener {
        void onRequestProgress(long contingLenth, long contentLength);
    }


}
