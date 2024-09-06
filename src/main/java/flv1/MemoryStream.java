package flv1;


import java.io.InputStream;

public class MemoryStream extends InputStream {
    public ReusableByteOutputStream data;
    protected int pos;
    protected int mark = 0;
    protected int count;

    public MemoryStream() {
        this.data = new ReusableByteOutputStream();
        this.pos = 0;
        this.count = 0;
    }
    
    public MemoryStream(byte[] buf, int count) {
        this.data = new ReusableByteOutputStream(buf, count);
        this.pos = 0;
        this.count = 0;
    }

    public synchronized int read() {
        return this.pos < this.count ? this.data.getBytes()[this.pos++] & 255 : -1;
    }

    public synchronized int read(byte[] b, int off, int len) {
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
                    System.arraycopy(this.data.getBytes(), this.pos, b, off, len);
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
    
    public synchronized void reset(int pos) {
        this.pos = pos;
        this.data.count = pos;
    }

    public void close() {
    }


    public void write(byte[] b, int off, int len) {
        data.count = pos; // 从 pos处写入！
        data.write(b, off, len);
        pos += len;
        count = Math.max(pos, count);
    }

    public void write(byte[] b) {
        this.write(b, 0, b.length);
    }

    public void write(int b) {
        data.count = pos; // 从 pos处写入！
        data.write(b);
        pos += 1;
        count = Math.max(pos, count);
    }

    public byte[] GetBuffer() {
        return data.getBytes();
    }

    public int Length() {
        return count = Math.max(count, pos);
    }

    public void Rebase(boolean end) {
        byte[] buffer = GetBuffer();
        int preserve = this.count - pos;
        System.arraycopy(buffer, pos, buffer, 0, preserve);
        this.count = preserve;
        this.data.count = preserve;
        this.pos = end?preserve:0;
    }
}