package flv1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class ReusableByteOutputStream extends OutputStream {
    protected byte[] buf;
    protected int count;
    private static final int MAX_ARRAY_SIZE = 2147483639;

    public byte[] data() {
        return this.buf;
    }

    public ReusableByteOutputStream() {
        this(1024);
    }

    public ReusableByteOutputStream(int size) {
        this.count = 0;
        this.buf = new byte[size];
    }

    public ReusableByteOutputStream(byte[] buf, int size) {
        this.count = 0;
        this.buf = buf;
        if (buf == null || buf.length != size) {
            this.buf = new byte[size];
        }

    }

    public void write(InputStream in, boolean breakOnZeroAvail) throws IOException {
        int cap;
        if (in instanceof ByteArrayInputStream) {
            cap = in.available();
            this.ensureCapacity(this.count + cap);
            this.count += in.read(this.buf, this.count, cap);
        } else {
            while(true) {
                cap = this.buf.length - this.count;
                int sz = in.read(this.buf, this.count, cap);
                if (sz < 0) {
                    return;
                }

                this.count += sz;
                if (cap == sz) {
                    int avail = in.available();
                    if (breakOnZeroAvail && avail == 0) {
                        break;
                    }

                    this.ensureCapacity(this.count + Math.max(64, in.available()));
                }
            }
        }

    }

    public void write(byte[] b, int off, int len) {
        if (off >= 0 && off <= b.length && len >= 0 && off + len - b.length <= 0) {
            this.ensureCapacity(this.count + len);
            System.arraycopy(b, off, this.buf, this.count, len);
            this.count += len;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public void write(byte[] b) {
        this.write(b, 0, b.length);
    }

    public void write(int b) {
        this.ensureCapacity(this.count + 1);
        this.buf[this.count] = (byte)b;
        ++this.count;
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity - this.buf.length > 0) {
            this.grow(minCapacity);
        }

    }

    private void grow(int minCapacity) {
        int oldCapacity = this.buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }

        if (newCapacity - 2147483639 > 0) {
            newCapacity = hugeCapacity(minCapacity);
        }

        this.buf = Arrays.copyOf(this.buf, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError();
        } else {
            return minCapacity > 2147483639 ? Integer.MAX_VALUE : 2147483639;
        }
    }

    public void reset() {
        this.count = 0;
    }

    /** @deprecated */
    public byte[] toByteArray() {
        byte[] newbuf = new byte[this.count];
        System.arraycopy(this.buf, 0, newbuf, 0, this.count);
        return newbuf;
    }

    public int size() {
        return this.count;
    }

    public String toString() {
        return new String(this.buf, 0, this.count);
    }

    public void close() {
    }

    public byte[] getBytes() {
        return this.buf;
    }

    public byte[] getArray(int planSize) {
        return this.toByteArray();
    }

    public byte[] getArray() {
        return this.buf.length == this.count ? this.buf : this.toByteArray();
    }

    public int getCount() {
        return this.count;
    }

    public void precede(int add) {
        if (this.count + add < this.buf.length) {
            this.count += add;
        }

    }

    public void recess(int sub) {
        if (this.count - sub > 0) {
            this.count -= sub;
        }

    }
}
