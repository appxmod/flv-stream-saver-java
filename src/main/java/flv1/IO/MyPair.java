package flv1.IO;

public class MyPair<T1, T2> {
    public T1 key;
    public T2 value;

    public MyPair(T1 k, T2 v) {
        this.key = k;
        this.value = v;
    }
}