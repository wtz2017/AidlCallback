package com.wtz.aidlcallback.server;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server extends Service {
    private static final String TAG = Server.class.getSimpleName();

    private CopyOnWriteArrayList<Student> mStudentList;

    private RemoteCallbackList<IOnRankChangedListener> mListeners;

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private static final int MSG_FIND_FIRST = 0;
    private static final int TIME_INTERVAL = 5000;

    private IStudentManager.Stub mBinder = new IStudentManager.Stub() {
        @Override
        public void addStudent(Student student) throws RemoteException {
            Log.d(TAG, "addStudent: " + student);
            if (student != null && student.getName() != null
                    && !exist(student)) {
                mStudentList.add(student);
            }
        }

        @Override
        public void deleteStudent(Student student) throws RemoteException {
            Log.d(TAG, "deleteStudent: " + student
            + ", mStudentList.contains: " + mStudentList.contains(student));
            for (Student s : mStudentList) {
                if (student.getName().equals(s.getName())) {
                    mStudentList.remove(s);
                    break;
                }
            }
        }

        @Override
        public boolean exist(Student student) throws RemoteException {
            if (student == null || student.getName() == null) {
                return false;
            }
            boolean exist = false;
            for (Student s : mStudentList) {
                if (student.getName().equals(s.getName())) {
                    exist = true;
                    break;
                }
            }
            return exist;
        }

        @Override
        public List<Student> getAllStudents() throws RemoteException {
            return mStudentList;
        }

        @Override
        public void registerOnRankChangedListener(IOnRankChangedListener listener) throws RemoteException {
            Log.d(TAG, "registerOnRankChangedListener: " + listener);
            mListeners.register(listener);
        }

        @Override
        public void unregisterOnRankChangedListener(IOnRankChangedListener listener) throws RemoteException {
            Log.d(TAG, "unregisterOnRankChangedListener: " + listener);
            mListeners.unregister(listener);
        }
    };

    public Server() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        mStudentList = new CopyOnWriteArrayList<>();
        mListeners = new RemoteCallbackList<>();

        mHandlerThread = new HandlerThread("handler_thread");
        mHandlerThread.start();
        mHandler = new Handler( mHandlerThread.getLooper() ){
            @Override
            public void handleMessage(Message msg) {
                Log.d( "mHandler " , "msg.what=" + msg.what) ;
                switch (msg.what) {
                    case MSG_FIND_FIRST:
                        Student first = findFirstStudent();
                        if (first != null) {
                            Log.d(TAG, "findFirstStudent: name=" + first.getName() + ", score=" + first.getScore()) ;
                            notifyClients(first);
                        }
                        removeMessages(MSG_FIND_FIRST);
                        mHandler.sendEmptyMessageDelayed(MSG_FIND_FIRST, TIME_INTERVAL);
                        break;
                }
            }
        };
        mHandler.sendEmptyMessageDelayed(MSG_FIND_FIRST, TIME_INTERVAL);
    }

    private Student findFirstStudent() {
        if (mStudentList == null || mStudentList.size() == 0) {
            return null;
        }
        int max = 0;
        int index = 0;
        int size = mStudentList.size();
        for (int i = 0; i < size; i++) {
            if (mStudentList.get(i).getScore() >= max) {
                index = i;
                max = mStudentList.get(i).getScore();
            }
        }
        return mStudentList.get(index);
    }

    private void notifyClients(Student first) {
        final int n = mListeners.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IOnRankChangedListener lis = mListeners.getBroadcastItem(i);
            try {
                if (lis != null) {
                    lis.onRankChanged(first);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mListeners.finishBroadcast();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: " + intent);
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
