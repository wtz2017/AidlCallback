package com.wtz.aidlcallback.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wtz.aidlcallback.R;
import com.wtz.aidlcallback.client.adapter.ListAdapter;
import com.wtz.aidlcallback.server.IOnRankChangedListener;
import com.wtz.aidlcallback.server.IStudentManager;
import com.wtz.aidlcallback.server.Student;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private EditText mInputName;
    private EditText mInputScore;
    private Button mAddButton;

    private ListView mlistView;
    private ListAdapter mListAdapter;

    private IStudentManager mStudentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        bindService();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        unregisterRemoteCallbacks();
        unbindService();
        super.onDestroy();
    }

    private void initView() {
        mInputName = (EditText) this.findViewById(R.id.et_name);
        mInputScore = (EditText) this.findViewById(R.id.et_score);
        mAddButton = (Button) this.findViewById(R.id.btn_add);
        mlistView = (ListView) this.findViewById(R.id.lv_list);

        mListAdapter = new ListAdapter(this, null);
        mlistView.setAdapter(mListAdapter);
        mlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                LayoutInflater lf = LayoutInflater.from(view.getContext());
                TextView tvInfo = (TextView) lf.inflate(R.layout.dialog_content, null);
                tvInfo.setText(mListAdapter.getList().get(position).getName());
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(MainActivity.this.getString(R.string.is_confirm_delete_record))
                        .setView(tvInfo)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                delelteStudent(mListAdapter.getList().get(position));
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // do nothing
                            }
                        }).show();
                return true;
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mInputName.getText())) {
                    toast("名字不能为空");
                    return;
                }

                if (TextUtils.isEmpty(mInputScore.getText())) {
                    toast("分数不能为空");
                    return;
                }

                addStudent();
            }
        });
    }

    private void delelteStudent(Student s) {
        try {
            mStudentManager.deleteStudent(s);
            mListAdapter.update(mStudentManager.getAllStudents());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void addStudent() {
        if (mStudentManager == null || !mStudentManager.asBinder().isBinderAlive()) {
            toast("请稍后！连接服务中...");
            return;
        }
        Student s = new Student();
        s.setName(mInputName.getText().toString());
        s.setScore(Integer.parseInt(mInputScore.getText().toString()));
        try {
            mStudentManager.addStudent(s);
            mListAdapter.update(mStudentManager.getAllStudents());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void toast(String msg) {
        Toast toast = Toast.makeText(MainActivity.this, "" + msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 200);
        toast.show();
    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.wtz.aidlcallback.server", "com.wtz.aidlcallback.server.Server"));
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void unbindService() {
        unbindService(mServiceConnection);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "onServiceConnected...Java ThreadId = " + Thread.currentThread().getId()
                    + ", Android Tid = " + android.os.Process.myTid());
            toast("服务已连接！");
            mStudentManager = IStudentManager.Stub.asInterface(iBinder);
            registerRemoteCallbacks();
            try {
                mListAdapter.update(mStudentManager.getAllStudents());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected...componentName = " + componentName);
            mStudentManager = null;
        }
    };

    private void registerRemoteCallbacks() {
        try {
            mStudentManager.registerOnRankChangedListener(mOnRankChangedListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void unregisterRemoteCallbacks() {
        if (mStudentManager != null && mStudentManager.asBinder().isBinderAlive()) {
            try {
                mStudentManager.unregisterOnRankChangedListener(mOnRankChangedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private IOnRankChangedListener.Stub mOnRankChangedListener = new IOnRankChangedListener.Stub() {
        @Override
        public void onRankChanged(final Student firstStudent) throws RemoteException {
            Log.i(TAG, "onRankChanged...name = " + firstStudent.getName()
                    + ", score = " + firstStudent.getScore()
                    + ", Java ThreadId = " + Thread.currentThread().getId()
                    + ", Android Tid = " + android.os.Process.myTid());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast("最新排名第一是" + firstStudent.getName() + ","
                            + firstStudent.getScore() + "分");
                }
            });
        }
    };

}
