// IOnRankChangedListener.aidl
package com.wtz.aidlcallback.server;

// Declare any non-default types here with import statements
import com.wtz.aidlcallback.server.Student;

interface IOnRankChangedListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onRankChanged(in Student firstStudent);
}
