package be.heh.pillule.activities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import SimaticS7.S7;
import SimaticS7.S7Client;
import be.heh.pillule.R;
import be.heh.pillule.ReadTaskS7;
import be.heh.pillule.WriteTaskS7;

public class LevelActivity extends Activity {

    private ReadTaskS7 readS7;
    private WriteTaskS7 writeDBB2;
    private WriteTaskS7 writeDBB3;
    private WriteTaskS7 writeDBW24;
    private WriteTaskS7 writeDBW26;
    private WriteTaskS7 writeDBW28;
    private WriteTaskS7 writeDBW30;

    private Resources resources;

    private int writePermission;

    private ProgressBar pb_level_S7;
    private TextView tv_level_automateName;
    private TextView tv_level_plc;
    private TextView tv_level_mode;
    private TextView tv_level_remoteConnection;
    private TextView tv_level_vanne1;
    private TextView tv_level_vanne2;
    private TextView tv_level_vanne3;
    private TextView tv_level_vanne4;
    private TextView tv_level_vanne1Label;
    private TextView tv_level_vanne2Label;
    private TextView tv_level_vanne3Label;
    private TextView tv_level_vanne4Label;
    private TextView tv_level_liquidLevel;
    private TextView tv_level_setpointAuto;
    private TextView tv_level_setpointManual;
    private TextView tv_level_wordOfValvePilot;
    private TableLayout tl_level_states;
    private Button btn_level_write;

    private CheckBox ch_level_dbb2_0;
    private CheckBox ch_level_dbb2_1;
    private CheckBox ch_level_dbb2_2;
    private CheckBox ch_level_dbb2_3;
    private CheckBox ch_level_dbb2_4;
    private CheckBox ch_level_dbb2_5;
    private CheckBox ch_level_dbb2_6;
    private CheckBox ch_level_dbb2_7;
    private CheckBox ch_level_dbb3_0;
    private CheckBox ch_level_dbb3_1;
    private CheckBox ch_level_dbb3_2;
    private CheckBox ch_level_dbb3_3;
    private CheckBox ch_level_dbb3_4;
    private CheckBox ch_level_dbb3_5;
    private CheckBox ch_level_dbb3_6;
    private CheckBox ch_level_dbb3_7;
    private EditText et_level_dbw24;
    private EditText et_level_dbw26;
    private EditText et_level_dbw28;
    private EditText et_level_dbw30;
    private Button btn_level_goBack;

    private String name, ip;
    private int rack, slot, databloc;

    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;

    private byte[] word = new byte[16];

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            networkInfo = connectivityManager.getActiveNetworkInfo();
            isNetworkActive();
            handler.postDelayed(this, 1000);
        }
    };

    private InfosReader infosReader;
    private boolean pause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        resources = getResources();

        writePermission = getIntent().getIntExtra("canWrite", -1);

        handler.postDelayed(runnable, 1000);

        isNetworkActive();

        pb_level_S7 = findViewById(R.id.pb_level_S7);
        tv_level_automateName = findViewById(R.id.tv_level_automateName);
        tv_level_plc = findViewById(R.id.tv_level_plc);
        ViewGroup parent = (ViewGroup) pb_level_S7.getParent();
        View view = getLayoutInflater().inflate(R.layout.level_reading, parent, false);
        parent.addView(view);
        initMainComponents();
        if (writePermission == 1)
            btn_level_write.setVisibility(View.VISIBLE);
        else
            btn_level_write.setVisibility(View.INVISIBLE);

        name = getIntent().getStringExtra("name");
        ip = getIntent().getStringExtra("ip");
        rack = getIntent().getIntExtra("rack", -1);
        slot = getIntent().getIntExtra("slot", -1);
        databloc = getIntent().getIntExtra("databloc", -1);

        tv_level_automateName.setText(name);

        readS7 = new ReadTaskS7(pb_level_S7, tv_level_plc);
        readS7.start(ip, String.valueOf(rack), String.valueOf(slot), String.valueOf(databloc));
        writeDBB2 = new WriteTaskS7(databloc, 2, 8);
        writeDBB3 = new WriteTaskS7(databloc, 3, 8);
        writeDBW24 = new WriteTaskS7(databloc, 24, 16);
        writeDBW26 = new WriteTaskS7(databloc, 26, 16);
        writeDBW28 = new WriteTaskS7(databloc, 28, 16);
        writeDBW30 = new WriteTaskS7(databloc, 30, 16);
        writeDBB2.start(ip, String.valueOf(rack), String.valueOf(slot));
        writeDBB3.start(ip, String.valueOf(rack), String.valueOf(slot));
        writeDBW24.start(ip, String.valueOf(rack), String.valueOf(slot));
        writeDBW26.start(ip, String.valueOf(rack), String.valueOf(slot));
        writeDBW28.start(ip, String.valueOf(rack), String.valueOf(slot));
        writeDBW30.start(ip, String.valueOf(rack), String.valueOf(slot));

        pause = false;
        infosReader = new InfosReader();
        infosReader.execute("");
    }

    private class InfosReader extends AsyncTask<String, Integer, String> {

        private S7Client client;

        private boolean isRunning;

        private String mode;
        private String remoteConnection;
        private String vanne1;
        private String vanne2;
        private String vanne3;
        private String vanne4;
        private String liquidLevel;
        private String setpointAuto;
        private String setpointManual;
        private String wordOfValvePilot;

        InfosReader() {
            client = new S7Client();
            isRunning = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            client.SetConnectionType(S7.S7_BASIC);
            if (ip != null)
                client.ConnectTo(ip, rack, slot);
            isRunning = true;
        }

        @Override
        protected String doInBackground(String... strings) {
            String string = "";
            while (isRunning) {
                while (!pause) {
                    byte[] dbb0 = new byte[8];
                    byte[] dbb1 = new byte[8];
                    byte[] dbw16 = new byte[2];
                    byte[] dbw18 = new byte[2];
                    byte[] dbw20 = new byte[2];
                    byte[] dbw22 = new byte[2];
                    int resultDbb0, resultDbb1, resultDbw16, resultDbw18, resultDbw20, resultDbw22;
                    resultDbb0 = client.ReadArea(S7.S7AreaDB, databloc, 0, 8, dbb0);
                    resultDbb1 = client.ReadArea(S7.S7AreaDB, databloc, 1, 8, dbb1);
                    resultDbw16 = client.ReadArea(S7.S7AreaDB, databloc, 16, 2, dbw16);
                    resultDbw18 = client.ReadArea(S7.S7AreaDB, databloc, 18, 2, dbw18);
                    resultDbw20 = client.ReadArea(S7.S7AreaDB, databloc, 20, 2, dbw20);
                    resultDbw22 = client.ReadArea(S7.S7AreaDB, databloc, 22, 2, dbw22);
                    if (resultDbb0 == 0 && resultDbb1 == 0 && resultDbw16 == 0 && resultDbw18 == 0 && resultDbw20 == 0 && resultDbw22 == 0) {
                        publishProgress(S7.GetBitAt(dbb0, 0, 1) ? 1 : 0,
                                S7.GetBitAt(dbb0, 0, 2) ? 1 : 0,
                                S7.GetBitAt(dbb0, 0, 3) ? 1 : 0,
                                S7.GetBitAt(dbb0, 0, 4) ? 1 : 0,
                                S7.GetBitAt(dbb0, 0, 5) ? 1 : 0,
                                S7.GetBitAt(dbb0, 0, 6) ? 1 : 0,
                                S7.GetBitAt(dbb1, 0, 1) ? 1 : 0,
                                S7.GetBitAt(dbb1, 0, 2) ? 1 : 0,
                                S7.GetBitAt(dbb1, 0, 3) ? 1 : 0,
                                S7.GetBitAt(dbb1, 0, 4) ? 1 : 0,
                                S7.GetWordAt(dbw16, 0),
                                S7.GetWordAt(dbw18, 0),
                                S7.GetWordAt(dbw20, 0),
                                S7.GetWordAt(dbw22, 0)
                        );
                    }
                    SystemClock.sleep(50);
                }
            }
            return string;
        }

        private void updateView() {
            if (vanne1.equals(resources.getString(R.string.On)))
                tv_level_vanne1.setTextColor(resources.getColor(R.color.green));
            else if (vanne1.equals(resources.getString(R.string.Off)))
                tv_level_vanne1.setTextColor(resources.getColor(R.color.red));
            tv_level_vanne1.setText(vanne1);

            if (vanne2.equals(resources.getString(R.string.On)))
                tv_level_vanne2.setTextColor(resources.getColor(R.color.green));
            else if (vanne2.equals(resources.getString(R.string.Off)))
                tv_level_vanne2.setTextColor(resources.getColor(R.color.red));
            tv_level_vanne2.setText(vanne2);

            if (vanne3.equals(resources.getString(R.string.On)))
                tv_level_vanne3.setTextColor(resources.getColor(R.color.green));
            else if (vanne3.equals(resources.getString(R.string.Off)))
                tv_level_vanne3.setTextColor(resources.getColor(R.color.red));
            tv_level_vanne3.setText(vanne3);

            if (vanne4.equals(resources.getString(R.string.On)))
                tv_level_vanne4.setTextColor(resources.getColor(R.color.green));
            else if (vanne4.equals(resources.getString(R.string.Off)))
                tv_level_vanne4.setTextColor(resources.getColor(R.color.red));
            tv_level_vanne4.setText(vanne4);

            tv_level_mode.setText(mode);

            if (remoteConnection.equals(resources.getString(R.string.On)))
                tv_level_remoteConnection.setTextColor(resources.getColor(R.color.green));
            else if (remoteConnection.equals(resources.getString(R.string.Off)))
                tv_level_remoteConnection.setTextColor(resources.getColor(R.color.red));
            tv_level_remoteConnection.setText(remoteConnection);

            tv_level_liquidLevel.setText(liquidLevel);
            tv_level_setpointAuto.setText(setpointAuto);
            tv_level_setpointManual.setText(setpointManual);
            tv_level_wordOfValvePilot.setText(wordOfValvePilot);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == 1)
                vanne1 = resources.getString(R.string.On);
            else
                vanne1 = resources.getString(R.string.Off);

            if (values[1] == 1)
                vanne2 = resources.getString(R.string.On);
            else
                vanne2 = resources.getString(R.string.Off);

            if (values[2] == 1)
                vanne3 = resources.getString(R.string.On);
            else
                vanne3 = resources.getString(R.string.Off);

            if (values[3] == 1)
                vanne4 = resources.getString(R.string.On);
            else
                vanne4 = resources.getString(R.string.Off);

            if (values[4] == 0)
                mode = resources.getString(R.string.Manual);
            else
                mode = resources.getString(R.string.Automatic);

            if (values[5] == 1)
                remoteConnection = resources.getString(R.string.On);
            else
                remoteConnection = resources.getString(R.string.Off);

            liquidLevel = String.valueOf(values[6]);
            setpointAuto = String.valueOf(values[7]);
            setpointManual = String.valueOf(values[8]);
            wordOfValvePilot = String.valueOf(values[9]);
            updateView();
        }

        @Override
        protected void onPostExecute(String s) {
            updateView();
        }

        void stop() {
            pause = true;
            isRunning = false;
            client.Disconnect();
        }
    }

    private void isNetworkActive() {
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            Toast.makeText(getApplicationContext(), R.string.errConnectionLost, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStop() {
        readS7.stop();
        writeDBB2.stop();
        writeDBB3.stop();
        writeDBW24.stop();
        writeDBW26.stop();
        writeDBW28.stop();
        writeDBW30.stop();
        handler.removeCallbacks(runnable);
        infosReader.stop();
        infosReader.cancel(true);
        super.onStop();
    }

    public void onPillClickManager(View v) {
        ViewGroup parent;
        View view;
        switch (v.getId()) {
            case R.id.btn_level_write:
                pause = true;
                parent = (ViewGroup) tl_level_states.getParent();
                parent.removeView(tl_level_states);
                view = getLayoutInflater().inflate(R.layout.level_writing, parent, false);
                parent.addView(view);
                initWriteComponents();
                break;
            case R.id.btn_level_goBack:
                parent = (ViewGroup) btn_level_goBack.getParent().getParent();
                parent.removeView((View) btn_level_goBack.getParent());
                view = getLayoutInflater().inflate(R.layout.level_reading, parent, false);
                parent.addView(view);
                initMainComponents();
                pause = false;
                break;
            case R.id.ch_level_dbb2_0:
                writeDBB2.setBitAt(7, ch_level_dbb2_0.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb2_1:
                writeDBB2.setBitAt(6, ch_level_dbb2_1.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb2_2:
                writeDBB2.setBitAt(5, ch_level_dbb2_2.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb2_3:
                writeDBB2.setBitAt(4, ch_level_dbb2_3.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb2_4:
                writeDBB2.setBitAt(3, ch_level_dbb2_4.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb2_5:
                writeDBB2.setBitAt(2, ch_level_dbb2_5.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb2_6:
                writeDBB2.setBitAt(1, ch_level_dbb2_6.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb2_7:
                writeDBB2.setBitAt(0, ch_level_dbb2_7.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb3_0:
                writeDBB2.setBitAt(7, ch_level_dbb3_0.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb3_1:
                writeDBB2.setBitAt(6, ch_level_dbb3_1.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb3_2:
                writeDBB2.setBitAt(5, ch_level_dbb3_2.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb3_3:
                writeDBB2.setBitAt(4, ch_level_dbb3_3.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb3_4:
                writeDBB2.setBitAt(3, ch_level_dbb3_4.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb3_5:
                writeDBB2.setBitAt(2, ch_level_dbb3_5.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb3_6:
                writeDBB2.setBitAt(1, ch_level_dbb3_6.isChecked() ? 1 : 0);
                break;
            case R.id.ch_level_dbb3_7:
                writeDBB2.setBitAt(0, ch_level_dbb3_7.isChecked() ? 1 : 0);
                break;
        }
    }

    private void initMainComponents() {
        tl_level_states = findViewById(R.id.tl_level_states);
        tv_level_mode = findViewById(R.id.tv_level_mode);
        tv_level_remoteConnection = findViewById(R.id.tv_level_remoteConnection);
        tv_level_vanne1 = findViewById(R.id.tv_level_vanne1);
        tv_level_vanne2 = findViewById(R.id.tv_level_vanne2);
        tv_level_vanne3 = findViewById(R.id.tv_level_vanne3);
        tv_level_vanne4 = findViewById(R.id.tv_level_vanne4);
        tv_level_vanne1Label = findViewById(R.id.tv_level_vanne1Label);
        tv_level_vanne2Label = findViewById(R.id.tv_level_vanne2Label);
        tv_level_vanne3Label = findViewById(R.id.tv_level_vanne3Label);
        tv_level_vanne4Label = findViewById(R.id.tv_level_vanne4Label);
        tv_level_liquidLevel = findViewById(R.id.tv_level_liquidLevel);
        tv_level_setpointAuto = findViewById(R.id.tv_level_setpointAuto);
        tv_level_setpointManual = findViewById(R.id.tv_level_setpointManual);
        tv_level_wordOfValvePilot = findViewById(R.id.tv_level_wordOfValvePilot);
        tv_level_vanne1Label.setText(String.format(resources.getString(R.string.ValveNumber), 1));
        tv_level_vanne2Label.setText(String.format(resources.getString(R.string.ValveNumber), 2));
        tv_level_vanne3Label.setText(String.format(resources.getString(R.string.ValveNumber), 3));
        tv_level_vanne4Label.setText(String.format(resources.getString(R.string.ValveNumber), 4));
        btn_level_write = findViewById(R.id.btn_level_write);
    }

    private void initWriteComponents() {
        ch_level_dbb2_0 = findViewById(R.id.ch_level_dbb2_0);
        ch_level_dbb2_1 = findViewById(R.id.ch_level_dbb2_1);
        ch_level_dbb2_2 = findViewById(R.id.ch_level_dbb2_2);
        ch_level_dbb2_3 = findViewById(R.id.ch_level_dbb2_3);
        ch_level_dbb2_4 = findViewById(R.id.ch_level_dbb2_4);
        ch_level_dbb2_5 = findViewById(R.id.ch_level_dbb2_5);
        ch_level_dbb2_6 = findViewById(R.id.ch_level_dbb2_6);
        ch_level_dbb2_7 = findViewById(R.id.ch_level_dbb2_7);
        ch_level_dbb3_0 = findViewById(R.id.ch_level_dbb3_0);
        ch_level_dbb3_1 = findViewById(R.id.ch_level_dbb3_1);
        ch_level_dbb3_2 = findViewById(R.id.ch_level_dbb3_2);
        ch_level_dbb3_3 = findViewById(R.id.ch_level_dbb3_3);
        ch_level_dbb3_4 = findViewById(R.id.ch_level_dbb3_4);
        ch_level_dbb3_5 = findViewById(R.id.ch_level_dbb3_5);
        ch_level_dbb3_6 = findViewById(R.id.ch_level_dbb3_6);
        ch_level_dbb3_7 = findViewById(R.id.ch_level_dbb3_7);
        et_level_dbw24 = findViewById(R.id.et_level_dbw24);
        et_level_dbw26 = findViewById(R.id.et_level_dbw26);
        et_level_dbw28 = findViewById(R.id.et_level_dbw28);
        et_level_dbw30 = findViewById(R.id.et_level_dbw30);
        et_level_dbw24.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    writeDBW24.setWordAt(0, Integer.parseInt(charSequence.toString()));
                } catch (NumberFormatException nFE) {
                    nFE.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_level_dbw26.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    writeDBW26.setWordAt(0, Integer.parseInt(charSequence.toString()));
                } catch (NumberFormatException nFE) {
                    nFE.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_level_dbw28.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    writeDBW28.setWordAt(0, Integer.parseInt(charSequence.toString()));
                } catch (NumberFormatException nFE) {
                    nFE.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_level_dbw30.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    writeDBW30.setWordAt(0, Integer.parseInt(charSequence.toString()));
                } catch (NumberFormatException nFE) {
                    nFE.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        btn_level_goBack = findViewById(R.id.btn_level_goBack);
    }

}
