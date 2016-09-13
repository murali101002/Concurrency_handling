package android.com.group18_inclass04;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.com.group18_inclass04.GeneratePassword;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView pwdCount, pwdLen, displayPwd;
    SeekBar pwdCountSeekbar, pwdLenSeekbar;
    Button threadButton, asyncButton;
    int cntProgress = 1, lenProgress = 8;
    ProgressDialog progressDialog;
    GeneratePassword generatePassword;
    List<String> listPasswords = new ArrayList<>();
    AlertDialog.Builder alertDialog;
    CharSequence[] cs;
    Handler handler;
    int setProgress;
    ArrayList<String> pwdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pwdCount = (TextView) findViewById(R.id.pwdCount);
        pwdCount.setText(String.valueOf(cntProgress));
        pwdLen = (TextView) findViewById(R.id.pwdLen);
        pwdLen.setText(String.valueOf(lenProgress));
        displayPwd = (TextView) findViewById(R.id.displayPwd);
        pwdCountSeekbar = (SeekBar) findViewById(R.id.pwdCountSeekbar);
        pwdCountSeekbar.setProgress(cntProgress);
        pwdLenSeekbar = (SeekBar) findViewById(R.id.pwdLenSeekbar);
        pwdLenSeekbar.setProgress(lenProgress);
        threadButton = (Button) findViewById(R.id.btn_thread);
        threadButton.setOnClickListener(this);
        asyncButton = (Button) findViewById(R.id.btn_async);
        asyncButton.setOnClickListener(this);
        pwdCountSeekbar.setMax(10);

        pwdCountSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) seekBar.setProgress(cntProgress);
                else cntProgress = progress;
                pwdCount.setText(String.valueOf(cntProgress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        pwdLenSeekbar.setMax(23);
        pwdLenSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 8) seekBar.setProgress(lenProgress);
                else lenProgress = progress;
                pwdLen.setText(String.valueOf(lenProgress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case GeneratePasswordsWithThreads.START:
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setMax(100);
                        progressDialog.setMessage("Generating Paswords...");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        break;
                    case GeneratePasswordsWithThreads.STEP:
                        progressDialog.setProgress((int)msg.obj);
                        break;
                    case GeneratePasswordsWithThreads.STOP:
                        pwdList = new ArrayList<>();
                        pwdList = (ArrayList<String>) msg.obj;
                        alertDialog = new AlertDialog.Builder(MainActivity.this);
                        progressDialog.dismiss();
                        cs = pwdList.toArray(new CharSequence[pwdList.size()]);
                        alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setItems(cs, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                displayPwd.setText(cs[which]);
                                listPasswords.clear();
                                cs = listPasswords.toArray(new CharSequence[listPasswords.size()]);
                            }
                        });
                        alertDialog.setTitle("Passwords");
                        alertDialog.setCancelable(false);
                        alertDialog.show();

                        break;
                }
                return false;
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_async:
                new GeneratePasswords().execute(cntProgress, lenProgress);
                break;
            case R.id.btn_thread:
                new Thread(new GeneratePasswordsWithThreads()).start();
                break;
        }

    }

    class GeneratePasswords extends AsyncTask<Integer, Integer, List<String>> {


        @Override
        protected List<String> doInBackground(Integer... params) {

            generatePassword = new GeneratePassword();
            for (int i = 1; i <= params[0]; i++) {
                listPasswords.add(generatePassword.getPassword(params[1]));
                publishProgress(100/(params[0]+1-i));
            }
            return listPasswords;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);
            progressDialog.dismiss();
            cs = strings.toArray(new CharSequence[strings.size()]);
            alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setItems(cs, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    displayPwd.setText(cs[which]);
                    listPasswords.clear();
                    cs = listPasswords.toArray(new CharSequence[listPasswords.size()]);
                }
            });
            alertDialog.setTitle("Passwords");
            alertDialog.setCancelable(false);
            alertDialog.show();

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMax(100);
            progressDialog.setMessage("Generating Paswords...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }
    }

    private class GeneratePasswordsWithThreads implements Runnable {
        static final int START = 0;
        static final int STEP = 1;
        static final int STOP = 2;

        @Override
        public void run() {
            Message msg = new Message();
            msg.what = START;
            handler.sendMessage(msg);
            for (int i = 1; i <= cntProgress; i++) {
                listPasswords.add(generatePassword.getPassword(lenProgress));
                msg = new Message();
                msg.what = STEP;
                msg.obj = 100/(cntProgress+1-i);
                handler.sendMessage(msg);
            }
            msg = new Message();
            msg.what = STOP;
            msg.obj = listPasswords;
            handler.sendMessage(msg);

        }
    }
}
