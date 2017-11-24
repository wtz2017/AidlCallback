// IStudentManager.aidl
package com.wtz.aidlcallback.server;

// Declare any non-default types here with import statements
import com.wtz.aidlcallback.server.Student;
import com.wtz.aidlcallback.server.IOnRankChangedListener;

interface IStudentManager {
    void addStudent(inout Student student);
    void deleteStudent(inout Student student);
    boolean exist(in Student student);
    List<Student> getAllStudents();
    void registerOnRankChangedListener(in IOnRankChangedListener listener);
    void unregisterOnRankChangedListener(in IOnRankChangedListener listener);
}
