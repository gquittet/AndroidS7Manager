package be.heh.pillule.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import be.heh.pillule.R;
import be.heh.pillule.adapters.AutomateManagerAdapter;
import be.heh.pillule.database.User;
import be.heh.pillule.objects.Automate;
import be.heh.pillule.objects.AutomateType;
import be.heh.pillule.security.Regex;

public class AutomateManagerActivity extends Activity implements SearchView.OnQueryTextListener {

    private Resources resources;

    private RecyclerView rv_user_adapter;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SearchView searchView;
    private AlertDialog dialog;
    private List<Automate> automateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automate_manager);

        resources = getApplicationContext().getResources();

        dialog = constructDialog(this);

        rv_user_adapter = findViewById(R.id.rv_user_automateList);
        rv_user_adapter.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        rv_user_adapter.setLayoutManager(mLayoutManager);

        automateList = new ArrayList<>();
        try {
            FileInputStream fIS;
            Gson gson = new Gson();
            fIS = openFileInput("automates.json");
            Type listType = new TypeToken<ArrayList<Automate>>() {
            }.getType();
            BufferedReader reader = new BufferedReader(new InputStreamReader(fIS));
            automateList = gson.fromJson(reader, listType);
            fIS.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        mAdapter = new AutomateManagerAdapter(automateList);
        rv_user_adapter.setAdapter(mAdapter);
    }

    private AlertDialog constructDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_automate_add, null);
        builder.setTitle(resources.getString(R.string.Add));
        builder.setView(view);
        final EditText et_dialog_automate_name = view.findViewById(R.id.et_dialog_automate_name);
        final EditText et_dialog_automate_ip = view.findViewById(R.id.et_dialog_automate_ip);
        final EditText et_dialog_automate_rack = view.findViewById(R.id.et_dialog_automate_rack);
        final EditText et_dialog_automate_slot = view.findViewById(R.id.et_dialog_automate_slot);
        final EditText et_dialog_automate_databloc = view.findViewById(R.id.et_dialog_automate_databloc);
        final Spinner sp_dialog_automate_type = view.findViewById(R.id.sp_dialog_automate_type);
        builder.setPositiveButton(R.string.Add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = et_dialog_automate_name.getText().toString();
                String ip = et_dialog_automate_ip.getText().toString();
                int rack = Integer.parseInt(et_dialog_automate_rack.getText().toString());
                int slot = Integer.parseInt(et_dialog_automate_slot.getText().toString());
                int databloc = Integer.parseInt(et_dialog_automate_databloc.getText().toString());
                String strType = sp_dialog_automate_type.getSelectedItem().toString();
                int type;
                if (strType.equals(resources.getString(R.string.Pill))) {
                    type = AutomateType.PILL;
                } else if (strType.equals(resources.getString(R.string.LiquidLevel))) {
                    type = AutomateType.LEVEL;
                } else {
                    type = AutomateType.ALL;
                }
                boolean error = false;
                if (name.isEmpty()) {
                    et_dialog_automate_name.setError(resources.getString(R.string.errNameInvalid));
                    error = true;
                }
                if (!Patterns.IP_ADDRESS.matcher(ip).matches()) {
                    et_dialog_automate_ip.setError(resources.getString(R.string.errIPInvalid));
                    error = true;
                }
                if (!Regex.isDigit(String.valueOf(rack))) {
                    et_dialog_automate_rack.setError(resources.getString(R.string.errRackInvalid));
                    error = true;
                }
                if (!Regex.isDigit(String.valueOf(slot))) {
                    et_dialog_automate_slot.setError(resources.getString(R.string.errSlotInvalid));
                    error = true;
                }
                if (!Regex.isDigit(String.valueOf(databloc))) {
                    et_dialog_automate_slot.setError(resources.getString(R.string.errDataBlocIncorrect));
                    error = true;
                }
                if (!error) {
                    List<Automate> list = ((AutomateManagerAdapter) mAdapter).getData();
                    Automate automate = new Automate(name, ip, rack, slot, databloc, type);
                    list.add(automate);
                    ((AutomateManagerAdapter) mAdapter).setData(list);
                    try {
                        Gson gson = new Gson();
                        FileOutputStream fos = openFileOutput("automates.json", Context.MODE_PRIVATE);
                        String automateStr = gson.toJson(list);
                        fos.write(automateStr.getBytes());
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });
        builder.setCancelable(false);
        builder.setNeutralButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return builder.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.automatemanagermenu, menu);
        searchView = (SearchView) menu.findItem(R.id.ab_user_automate_search).getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ab_user_automate_quit:
                quit();
                return true;
            case R.id.ab_user_automate_add:
                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void quit() {
        Intent intent = new Intent(getApplicationContext(), UserActivity.class);
        intent.putExtra("canWrite", getIntent().getIntExtra("canWrite", -1));
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        ((AutomateManagerAdapter) mAdapter).filter(s);
        return false;
    }

    @Override
    public void onBackPressed() {
        quit();
    }
}
