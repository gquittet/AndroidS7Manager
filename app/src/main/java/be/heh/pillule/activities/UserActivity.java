package be.heh.pillule.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import be.heh.pillule.R;
import be.heh.pillule.adapters.UserAdapter;
import be.heh.pillule.objects.Automate;

public class UserActivity extends Activity {

    private Resources resources;

    private List<Automate> automateList;

    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        resources = getApplicationContext().getResources();


        int[] pictures = new int[]{
                R.drawable.automate,
                R.drawable.drugs,
                R.drawable.level
        };
        String[][] datas = new String[][]{
                {resources.getString(R.string.Automate), resources.getString(R.string.userMenuInfoAutomate), resources.getString(R.string.userMenuButtonAutomate)},
                {resources.getString(R.string.Pill), resources.getString(R.string.userMenuInfoPill), resources.getString(R.string.userMenuButtonPill)},
                {resources.getString(R.string.LiquidLevel), resources.getString(R.string.userMenuInfoLiquidLevel), resources.getString(R.string.userMenuButtonLiquidLevel)}
        };

        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        networkInfo = connectivityManager.getActiveNetworkInfo();


        RecyclerView rv_user_menu = findViewById(R.id.rv_user_menuList);
        rv_user_menu.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        rv_user_menu.setLayoutManager(mLayoutManager);

        RecyclerView.Adapter mAdapter = new UserAdapter(pictures, datas);
        rv_user_menu.setAdapter(mAdapter);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onUserMenuClickManager(View v) {
        networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Button button = (Button) v;
            String buttonText = button.getText().toString();
            Intent intent;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            @SuppressLint("InflateParams") View dialogView = layoutInflater.inflate(R.layout.dialog_automate_choose, null);
            builder.setTitle(R.string.Connection);
            builder.setView(dialogView);
            builder.setCancelable(true);
            final Spinner sp_dialog_automate_choose = dialogView.findViewById(R.id.sp_dialog_automate_choose);
            List<String> list = new ArrayList<>();
            builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            if (buttonText.equals(resources.getString(R.string.userMenuButtonAutomate))) {
                intent = new Intent(getApplicationContext(), AutomateManagerActivity.class);
                intent.putExtra("canWrite", getIntent().getIntExtra("canWrite", -1));
                startActivity(intent);
                finish();
            } else if (buttonText.equals(resources.getString(R.string.userMenuButtonPill))) {
                for (Automate automate : automateList) {
                    if (automate.getType() == 0 || automate.getType() == 1)
                        list.add(automate.getName());
                }
                createSpinner(list, sp_dialog_automate_choose);
                finalizeConnectionDialog(list, sp_dialog_automate_choose, builder, PillActivity.class);
            } else if (buttonText.equals(resources.getString(R.string.userMenuButtonLiquidLevel))) {
                for (Automate automate : automateList) {
                    if (automate.getType() == 0 || automate.getType() == 2)
                        list.add(automate.getName());
                }
                createSpinner(list, sp_dialog_automate_choose);
                finalizeConnectionDialog(list, sp_dialog_automate_choose, builder, LevelActivity.class);
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.errConnection, Toast.LENGTH_SHORT).show();
        }
    }

    private void createSpinner(List<String> list, Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, list);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);
    }

    private void finalizeConnectionDialog(List<String> list, final Spinner spinner, AlertDialog.Builder builder, final Class<?> cls) {
        if (list.size() > 0) {
            builder.setPositiveButton(R.string.Connection, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    for (Automate automate : automateList) {
                        if (automate.getName().equals(spinner.getSelectedItem().toString())) {
                            try {
                                if (Runtime.getRuntime().exec("/system/bin/ping -c 1 " + automate.getIp()).waitFor() == 0) {
                                    Intent intent = new Intent(getApplicationContext(), cls);
                                    intent.putExtra("name", automate.getName());
                                    intent.putExtra("ip", automate.getIp());
                                    intent.putExtra("rack", automate.getRack());
                                    intent.putExtra("slot", automate.getSlot());
                                    intent.putExtra("databloc", automate.getDatabloc());
                                    intent.putExtra("canWrite", getIntent().getIntExtra("canWrite", -1));
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(UserActivity.this, R.string.errAutomateNotFound, Toast.LENGTH_SHORT).show();
                                }
                            } catch (InterruptedException | IOException e) {
                                e.printStackTrace();
                                Log.e("[Error]:", e.getMessage());
                            }
                        }
                    }
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.errNoAutomate, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.usermenu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkInfo = connectivityManager.getActiveNetworkInfo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ab_usermenu_disconnect:
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        startActivity(intent);
        finish();
    }
}
