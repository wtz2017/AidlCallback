package com.wtz.aidlcallback.server;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by WTZ on 2017/11/22.
 */

public class Student implements Parcelable {

    String name;
    int score;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Student() {}

    protected Student(Parcel in) {
        name = in.readString();
        score = in.readInt();
    }

    /**
     * 此方法是默认方法，支持 in 的定向 tag
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(score);
    }

    /**
     * 此方法是额外添加，为了支持 out 或者 inout 的定向 tag
     * @param dest
     */
    public void readFromParcel(Parcel dest) {
        //注意，此处的读值顺序应当是和writeToParcel()方法中一致的
        name = dest.readString();
        score = dest.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Student> CREATOR = new Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel in) {
            return new Student(in);
        }

        @Override
        public Student[] newArray(int size) {
            return new Student[size];
        }
    };

    @Override
    public String toString() {
        return "Student{name=" + name + ",score=" + score + "}";
    }

}
