package flv1;

import java.io.InputStream;

import static flv1.IO.VU.debug;

public class ReusableByteInputStream extends InputStream {
    protected byte[] buf;
    protected int pos;
    protected int mark = 0;
    protected int count;

    public ReusableByteInputStream(byte[] buf) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }

    public ReusableByteInputStream(byte[] buf, int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.mark = offset;
    }

    public synchronized int read() {
//        debug("xxread::", pos, 1);
        return this.pos < this.count ? this.buf[this.pos++] & 255 : -1;
    }

    public synchronized int read(byte[] b, int off, int len) {
//        debug("xxread::", pos, len);
//        if (len==4 || len==2) {
//            try {
//            	throw new RuntimeException(len+" watch stacktrace!");
//            } catch (RuntimeException e) {
//            	debug(e);
//            }
//        }
        if (b == null) {
            throw new NullPointerException();
        } else if (off >= 0 && len >= 0 && len <= b.length - off) {
            if (this.pos >= this.count) {
                return -1;
            } else {
                int avail = this.count - this.pos;
                if (len > avail) {
                    len = avail;
                }

                if (len <= 0) {
                    return 0;
                } else {
                    System.arraycopy(this.buf, this.pos, b, off, len);
                    this.pos += len;
                    return len;
                }
            }
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public synchronized long skip(long n) {
        long k = (long)(this.count - this.pos);
        if (n < k) {
            k = n < 0L ? 0L : n;
        }

        this.pos = (int)((long)this.pos + k);
        return k;
    }

    public synchronized int available() {
        return this.count - this.pos;
    }

    public boolean markSupported() {
        return true;
    }

    public void mark(int readAheadLimit) {
        this.mark = this.pos;
    }

    public synchronized void reset() {
        this.pos = this.mark;
    }

    public void close() {
    }
}
