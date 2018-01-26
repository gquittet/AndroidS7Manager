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

public class PillActivity extends Activity {

    private ReadTaskS7 readS7;
    private WriteTaskS7 writeDBB5;
    private WriteTaskS7 writeDBB6;
    private WriteTaskS7 writeDBB7;
    private WriteTaskS7 writeDBB8;
    private WriteTaskS7 writeDBW18;

    private Resources resources;

    private ProgressBar pb_pill_S7;
    private TextView tv_pill_automateName;
    private TextView tv_pill_plc;
    private TextView tv_pill_bottleNumber;
    private TextView tv_pill_pillNumber;
    private TextView tv_pill_runState;
    private TextView tv_pill_genBottlesState;
    private TextView tv_pill_pillAsk;
    private TextView tv_pill_localRemote;
    private TextView tv_pill_enginePill;
    private TextView tv_pill_engineStrip;
    private TextView tv_pill_plug;
    private Button btn_pill_write;
    private Button btn_pill_goBack;
    private TableLayout tl_pill_states;

    private CheckBox ch_pill_dbb5_0;
    private CheckBox ch_pill_dbb5_1;
    private CheckBox ch_pill_dbb5_2;
    private CheckBox ch_pill_dbb5_3;
    private CheckBox ch_pill_dbb5_4;
    private CheckBox ch_pill_dbb5_5;
    private CheckBox ch_pill_dbb5_6;
    private CheckBox ch_pill_dbb5_7;
    private CheckBox ch_pill_dbb6_0;
    private CheckBox ch_pill_dbb6_1;
    private CheckBox ch_pill_dbb6_2;
    private CheckBox ch_pill_dbb6_3;
    private CheckBox ch_pill_dbb6_4;
    private CheckBox ch_pill_dbb6_5;
    private CheckBox ch_pill_dbb6_6;
    private CheckBox ch_pill_dbb6_7;
    private CheckBox ch_pill_dbb7_0;
    private CheckBox ch_pill_dbb7_1;
    private CheckBox ch_pill_dbb7_2;
    private CheckBox ch_pill_dbb7_3;
    private CheckBox ch_pill_dbb7_4;
    private CheckBox ch_pill_dbb7_5;
    private CheckBox ch_pill_dbb7_6;
    private CheckBox ch_pill_dbb7_7;
    private EditText et_pill_dbb8;
    private EditText et_pill_dbw18;
    private String binaryValue;

    private int writePermission;

    private boolean pause;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        writePermission = getIntent().getIntExtra("canWrite", -1);

        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        networkInfo = connectivityManager.getActiveNetworkInfo();

        resources = getResources();

        handler.postDelayed(runnable, 1000);

        isNetworkActive();

        pb_pill_S7 = findViewById(R.id.pb_pill_S7);
        tv_pill_automateName = findViewById(R.id.tv_pill_automateName);
        tv_pill_plc = findViewById(R.id.tv_pill_plc);
        ViewGroup parent = (ViewGroup) pb_pill_S7.getParent();
        View view = getLayoutInflater().inflate(R.layout.pill_reading, parent, false);
        parent.addView(view);
        initMainComponents();
        if (writePermission == 1)
            btn_pill_write.setVisibility(View.VISIBLE);
        else
            btn_pill_write.setVisibility(View.INVISIBLE);

        name = getIntent().getStringExtra("name");
        ip = getIntent().getStringExtra("ip");
        rack = getIntent().getIntExtra("rack", -1);
        slot = getIntent().getIntExtra("slot", -1);
        databloc = getIntent().getIntExtra("databloc", -1);

        tv_pill_automateName.setText(name);

        readS7 = new ReadTaskS7(pb_pill_S7, tv_pill_plc);
        readS7.start(ip, String.valueOf(rack), String.valueOf(slot), String.valueOf(databloc));
        writeDBB5 = new WriteTaskS7(databloc, 5, 8);
        writeDBB6 = new WriteTaskS7(databloc, 6, 8);
        writeDBB7 = new WriteTaskS7(databloc, 7, 8);
        writeDBB8 = new WriteTaskS7(databloc, 8, 8);
        writeDBW18 = new WriteTaskS7(databloc, 18, 16);
        writeDBB5.start(ip, String.valueOf(rack), String.valueOf(slot));
        writeDBB6.start(ip, String.valueOf(rack), String.valueOf(slot));
        writeDBB7.start(ip, String.valueOf(rack), String.valueOf(slot));
        writeDBB8.start(ip, String.valueOf(rack), String.valueOf(slot));
        writeDBW18.start(ip, String.valueOf(rack), String.valueOf(slot));

        pause = false;
        infosReader = new InfosReader();
        infosReader.execute("");
    }

    private class InfosReader extends AsyncTask<String, Integer, String> {

        private S7Client client;

        private String bottles;
        private String pills;
        private String running;
        private String genBottles;
        private String pillAsk;
        private String localRemote;
        private String enginePill;
        private String engineStrip;
        private String plugState;

        private boolean isRunning;

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
                    byte[] bottleNumber = new byte[2];
                    byte[] dbb15 = new byte[8];
                    byte[] dbb151 = new byte[8];
                    byte[] dbb0 = new byte[8];
                    byte[] dbb1 = new byte[8];
                    byte[] dbb4 = new byte[8];
                    int resultB, resultDBB15, resultDBB151, resultDBB0, resultDBB1, resultDBB4;
                    resultB = client.ReadArea(S7.S7AreaDB, databloc, 16, 2, bottleNumber);
                    resultDBB15 = client.ReadArea(S7.S7AreaDB, databloc, 14, 8, dbb15);
                    resultDBB151 = client.ReadArea(S7.S7AreaDB, databloc, 15, 8, dbb151);
                    resultDBB0 = client.ReadArea(S7.S7AreaDB, databloc, 0, 8, dbb0);
                    resultDBB1 = client.ReadArea(S7.S7AreaDB, databloc, 1, 8, dbb1);
                    resultDBB4 = client.ReadArea(S7.S7AreaDB, databloc, 4, 8, dbb4);
                    if (resultB == 0 && resultDBB15 == 0 && resultDBB151 == 0 && resultDBB0 == 0 && resultDBB1 == 0 && resultDBB4 == 0) {
                        publishProgress(S7.GetBitAt(dbb15, 0, 0) ? 1 : 0,
                                S7.GetBitAt(dbb15, 0, 1) ? 1 : 0,
                                S7.GetBitAt(dbb15, 0, 2) ? 1 : 0,
                                S7.GetBitAt(dbb15, 0, 3) ? 1 : 0,
                                S7.GetBitAt(dbb15, 0, 4) ? 1 : 0,
                                S7.GetBitAt(dbb15, 0, 5) ? 1 : 0,
                                S7.GetBitAt(dbb15, 0, 6) ? 1 : 0,
                                S7.GetBitAt(dbb15, 0, 7) ? 1 : 0,
                                S7.GetBitAt(dbb151, 0, 0) ? 1 : 0,
                                S7.GetBitAt(dbb151, 0, 1) ? 1 : 0,
                                S7.GetBitAt(dbb151, 0, 2) ? 1 : 0,
                                S7.GetBitAt(dbb151, 0, 3) ? 1 : 0,
                                S7.GetBitAt(dbb151, 0, 4) ? 1 : 0,
                                S7.GetBitAt(dbb151, 0, 5) ? 1 : 0,
                                S7.GetBitAt(dbb151, 0, 6) ? 1 : 0,
                                S7.GetBitAt(dbb151, 0, 7) ? 1 : 0,
                                S7.GetBitAt(dbb0, 0, 0) ? 1 : 0,
                                S7.GetBitAt(dbb1, 0, 3) ? 1 : 0,
                                S7.GetBitAt(dbb4, 0, 3) ? 1 : 0,
                                S7.GetBitAt(dbb4, 0, 4) ? 1 : 0,
                                S7.GetBitAt(dbb4, 0, 5) ? 1 : 0,
                                S7.GetWordAt(bottleNumber, 0),
                                S7.GetBitAt(dbb1, 0, 6) ? 1 : 0,
                                S7.GetBitAt(dbb4, 0, 0) ? 1 : 0,
                                S7.GetBitAt(dbb4, 0, 1) ? 1 : 0,
                                S7.GetBitAt(dbb4, 0, 2) ? 1 : 0
                        );
                    }
                    SystemClock.sleep(100);
                }
            }
            return string;
        }

        private void updateView() {
            tv_pill_bottleNumber.setText(bottles);
            tv_pill_pillNumber.setText(pills);

            if (running.equals(resources.getString(R.string.On)))
                tv_pill_runState.setTextColor(resources.getColor(R.color.green));
            else if (running.equals(resources.getString(R.string.Off)))
                tv_pill_runState.setTextColor(resources.getColor(R.color.red));
            tv_pill_runState.setText(running);

            if (genBottles.equals(resources.getString(R.string.On)))
                tv_pill_genBottlesState.setTextColor(resources.getColor(R.color.green));
            else if (genBottles.equals(resources.getString(R.string.Off)))
                tv_pill_genBottlesState.setTextColor(resources.getColor(R.color.red));
            tv_pill_genBottlesState.setText(genBottles);

            tv_pill_pillAsk.setText(pillAsk);

            if (localRemote.equals(resources.getString(R.string.On)))
                tv_pill_localRemote.setTextColor(resources.getColor(R.color.green));
            else if (localRemote.equals(resources.getString(R.string.Off)))
                tv_pill_localRemote.setTextColor(resources.getColor(R.color.red));
            tv_pill_localRemote.setText(localRemote);

            if (enginePill.equals(resources.getString(R.string.On)))
                tv_pill_enginePill.setTextColor(resources.getColor(R.color.green));
            else if (enginePill.equals(resources.getString(R.string.Off)))
                tv_pill_enginePill.setTextColor(resources.getColor(R.color.red));
            tv_pill_enginePill.setText(enginePill);

            if (engineStrip.equals(resources.getString(R.string.On)))
                tv_pill_engineStrip.setTextColor(resources.getColor(R.color.green));
            else if (engineStrip.equals(resources.getString(R.string.Off)))
                tv_pill_engineStrip.setTextColor(resources.getColor(R.color.red));
            tv_pill_engineStrip.setText(engineStrip);

            if (plugState.equals(resources.getString(R.string.On)))
                tv_pill_plug.setTextColor(resources.getColor(R.color.green));
            else if (plugState.equals(resources.getString(R.string.Off)))
                tv_pill_plug.setTextColor(resources.getColor(R.color.red));
            tv_pill_plug.setText(plugState);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            StringBuilder number0 = new StringBuilder();
            StringBuilder number1 = new StringBuilder();
            for (int i = 7; i >= 0; i--) {
                number0.append(values[i]);
                number1.append(values[i + 8]);
            }
            pills = Integer.toString(Integer.parseInt(number0.toString() + number1.toString(), 2));
            bottles = String.valueOf(values[21]);
            if (values[16] == 1)
                running = resources.getString(R.string.On);
            else
                running = resources.getString(R.string.Off);
            if (values[17] == 1)
                genBottles = resources.getString(R.string.On);
            else
                genBottles = resources.getString(R.string.Off);
            if (values[18] == 1)
                pillAsk = "5";
            else if (values[19] == 1)
                pillAsk = "10";
            else if (values[20] == 1)
                pillAsk = "15";
            else
                pillAsk = "0";
            if (values[22] == 1)
                localRemote = resources.getString(R.string.On);
            else
                localRemote = resources.getString(R.string.Off);
            if (values[23] == 1)
                enginePill = resources.getString(R.string.On);
            else
                enginePill = resources.getString(R.string.Off);
            if (values[24] == 1)
                engineStrip = resources.getString(R.string.On);
            else
                engineStrip = resources.getString(R.string.Off);
            if (values[25] == 1)
                plugState = resources.getString(R.string.On);
            else
                plugState = resources.getString(R.string.Off);
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
        writeDBB5.stop();
        writeDBB6.stop();
        writeDBB7.stop();
        writeDBB8.stop();
        writeDBW18.stop();
        handler.removeCallbacks(runnable);
        infosReader.stop();
        infosReader.cancel(true);
        super.onStop();
    }

    public void onPillClickManager(View v) {
        ViewGroup parent;
        View view;
        switch (v.getId()) {
            case R.id.btn_pill_write:
                pause = true;
                parent = (ViewGroup) tl_pill_states.getParent();
                parent.removeView(tl_pill_states);
                view = getLayoutInflater().inflate(R.layout.pill_writing, parent, false);
                parent.addView(view);
                initWriteComponents();
                break;
            case R.id.btn_pill_goBack:
                parent = (ViewGroup) btn_pill_goBack.getParent().getParent();
                parent.removeView((View) btn_pill_goBack.getParent());
                view = getLayoutInflater().inflate(R.layout.pill_reading, parent, false);
                parent.addView(view);
                initMainComponents();
                pause = false;
                break;
            case R.id.ch_pill_dbb5_0:
                writeDBB5.setBitAt(7, ch_pill_dbb5_0.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb5_1:
                writeDBB5.setBitAt(6, ch_pill_dbb5_1.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb5_2:
                writeDBB5.setBitAt(5, ch_pill_dbb5_2.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb5_3:
                writeDBB5.setBitAt(4, ch_pill_dbb5_3.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb5_4:
                writeDBB5.setBitAt(3, ch_pill_dbb5_4.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb5_5:
                writeDBB5.setBitAt(2, ch_pill_dbb5_5.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb5_6:
                writeDBB5.setBitAt(1, ch_pill_dbb5_6.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb5_7:
                writeDBB5.setBitAt(0, ch_pill_dbb5_7.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb6_0:
                writeDBB6.setBitAt(7, ch_pill_dbb6_0.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb6_1:
                writeDBB6.setBitAt(6, ch_pill_dbb6_1.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb6_2:
                writeDBB6.setBitAt(5, ch_pill_dbb6_2.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb6_3:
                writeDBB6.setBitAt(4, ch_pill_dbb6_3.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb6_4:
                writeDBB6.setBitAt(3, ch_pill_dbb6_4.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb6_5:
                writeDBB6.setBitAt(2, ch_pill_dbb6_5.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb6_6:
                writeDBB6.setBitAt(1, ch_pill_dbb6_6.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb6_7:
                writeDBB6.setBitAt(0, ch_pill_dbb6_7.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb7_0:
                writeDBB7.setBitAt(7, ch_pill_dbb7_0.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb7_1:
                writeDBB7.setBitAt(6, ch_pill_dbb7_1.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb7_2:
                writeDBB7.setBitAt(5, ch_pill_dbb7_2.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb7_3:
                writeDBB7.setBitAt(4, ch_pill_dbb7_3.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb7_4:
                writeDBB7.setBitAt(3, ch_pill_dbb7_4.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb7_5:
                writeDBB7.setBitAt(2, ch_pill_dbb7_5.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb7_6:
                writeDBB7.setBitAt(1, ch_pill_dbb7_6.isChecked() ? 1 : 0);
                break;
            case R.id.ch_pill_dbb7_7:
                writeDBB7.setBitAt(0, ch_pill_dbb7_7.isChecked() ? 1 : 0);
                break;
        }
    }

    private void initMainComponents() {
        tv_pill_bottleNumber = findViewById(R.id.tv_pill_bottleNumber);
        tv_pill_pillNumber = findViewById(R.id.tv_pill_pillNumber);
        tv_pill_runState = findViewById(R.id.tv_pill_runState);
        tv_pill_genBottlesState = findViewById(R.id.tv_pill_genBottlesState);
        tv_pill_pillAsk = findViewById(R.id.tv_pill_pillAsk);
        tv_pill_localRemote = findViewById(R.id.tv_pill_localRemote);
        tv_pill_enginePill = findViewById(R.id.tv_pill_enginePill);
        tv_pill_engineStrip = findViewById(R.id.tv_pill_engineStrip);
        tv_pill_plug = findViewById(R.id.tv_pill_plug);
        btn_pill_write = findViewById(R.id.btn_pill_write);
        tl_pill_states = findViewById(R.id.tl_pill_states);
    }

    private void initWriteComponents() {
        ch_pill_dbb5_0 = findViewById(R.id.ch_pill_dbb5_0);
        ch_pill_dbb5_1 = findViewById(R.id.ch_pill_dbb5_1);
        ch_pill_dbb5_2 = findViewById(R.id.ch_pill_dbb5_2);
        ch_pill_dbb5_3 = findViewById(R.id.ch_pill_dbb5_3);
        ch_pill_dbb5_4 = findViewById(R.id.ch_pill_dbb5_4);
        ch_pill_dbb5_5 = findViewById(R.id.ch_pill_dbb5_5);
        ch_pill_dbb5_6 = findViewById(R.id.ch_pill_dbb5_6);
        ch_pill_dbb5_7 = findViewById(R.id.ch_pill_dbb5_7);
        ch_pill_dbb6_0 = findViewById(R.id.ch_pill_dbb6_0);
        ch_pill_dbb6_1 = findViewById(R.id.ch_pill_dbb6_1);
        ch_pill_dbb6_2 = findViewById(R.id.ch_pill_dbb6_2);
        ch_pill_dbb6_3 = findViewById(R.id.ch_pill_dbb6_3);
        ch_pill_dbb6_4 = findViewById(R.id.ch_pill_dbb6_4);
        ch_pill_dbb6_5 = findViewById(R.id.ch_pill_dbb6_5);
        ch_pill_dbb6_6 = findViewById(R.id.ch_pill_dbb6_6);
        ch_pill_dbb6_7 = findViewById(R.id.ch_pill_dbb6_7);
        ch_pill_dbb7_0 = findViewById(R.id.ch_pill_dbb7_0);
        ch_pill_dbb7_1 = findViewById(R.id.ch_pill_dbb7_1);
        ch_pill_dbb7_2 = findViewById(R.id.ch_pill_dbb7_2);
        ch_pill_dbb7_3 = findViewById(R.id.ch_pill_dbb7_3);
        ch_pill_dbb7_4 = findViewById(R.id.ch_pill_dbb7_4);
        ch_pill_dbb7_5 = findViewById(R.id.ch_pill_dbb7_5);
        ch_pill_dbb7_6 = findViewById(R.id.ch_pill_dbb7_6);
        ch_pill_dbb7_7 = findViewById(R.id.ch_pill_dbb7_7);
        et_pill_dbb8 = findViewById(R.id.et_pill_dbb8);
        et_pill_dbw18 = findViewById(R.id.et_pill_dbw18);
        et_pill_dbb8.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                treatNumber(charSequence, 8, writeDBB8);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_pill_dbw18.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    writeDBW18.setWordAt(0, Integer.parseInt(charSequence.toString()));
                } catch (NumberFormatException nFE) {
                    nFE.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        btn_pill_goBack = findViewById(R.id.btn_pill_goBack);
    }

    private void treatNumber(CharSequence charSequence, int size, WriteTaskS7 writeTask) {
        try {
            if (charSequence.toString().length() == 0 || Integer.parseInt(charSequence.toString()) > Math.pow(2, size) - 1) {
                binaryValue = "";
            } else {
                binaryValue = Integer.toBinaryString(Integer.parseInt(charSequence.toString()));
                int clock = 0;
                for (int k = binaryValue.length() - 1; k >= 0; k--) {
                    writeTask.setBitAt(clock++, Integer.parseInt(Character.toString(binaryValue.charAt(k))));
                    if (clock > size - 1)
                        break;
                }
            }
        } catch (NumberFormatException nFE) {
            nFE.printStackTrace();
        }
    }

}
