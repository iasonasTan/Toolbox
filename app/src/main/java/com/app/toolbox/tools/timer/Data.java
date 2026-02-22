package com.app.toolbox.tools.timer;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Objects;

public final class Data implements Parcelable {
    public static Data check(Data d) {
        if(d==null||d.ids==null||d.titles==null||d.deltas==null)
            throw new NullPointerException("Members of this are null");
        if(!(d.ids.length==d.titles.length&&
                d.titles.length==d.deltas.length))
            throw new RuntimeException();
        return d;
    }

    public final int[] ids;
    public final String[] titles;
    public final long[] deltas;

    public Data(int n) {
        this(new int[n], new String[n], new long[n]);
    }

    public Data(int[] ids, String[] titles, long[] deltas) {
        this.ids = ids;
        this.titles = titles;
        this.deltas = deltas;
    }

    private Data(Parcel in) {
        ids = in.createIntArray();
        titles = in.createStringArray();
        deltas = in.createLongArray();
    }

    public int len(){
        return ids.length;
    }

    public static final Creator<Data> CREATOR = new Creator<>() {
        @Override
        public Data createFromParcel(Parcel in) {
            return new Data(in);
        }

        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return Objects.deepEquals(ids, data.ids) && Objects.deepEquals(titles, data.titles) && Objects.deepEquals(deltas, data.deltas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(ids), Arrays.hashCode(titles), Arrays.hashCode(deltas));
    }

    @NonNull
    @Override
    public String toString() {
        return "Data{" +
                "ids=" + Arrays.toString(ids) +
                ", titles=" + Arrays.toString(titles) +
                ", deltas=" + Arrays.toString(deltas) +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeIntArray(ids);
        dest.writeStringArray(titles);
        dest.writeLongArray(deltas);
    }
}
