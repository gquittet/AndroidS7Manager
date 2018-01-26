package be.heh.pillule.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.heh.pillule.R;
import be.heh.pillule.objects.Automate;
import be.heh.pillule.objects.AutomateType;
import be.heh.pillule.security.Regex;

/**
 * Created by gquittet on 12/19/17.
 */

public class AutomateManagerAdapter extends RecyclerView.Adapter<AutomateManagerAdapter.AutomateViewHolder> {

    private Resources resources;

    private List<Automate> automateList;
    private List<Automate> searchList;

    public AutomateManagerAdapter(List<Automate> automateList) {
        setData(automateList);
    }

    @Override
    public AutomateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.rv_user_automate, parent, false);
        return new AutomateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AutomateViewHolder holder, int position) {
        holder.display(searchList.get(position));
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public void filter(String s) {
        if (s.length() == 0) {
          searchList = automateList;
        } else {
            searchList = new ArrayList<>();
            for (Automate automate : automateList) {
                String type = "";
                if (automate.getType() == AutomateType.PILL)
                    type = "Pill";
                else if (automate.getType() == AutomateType.LEVEL)
                    type = "Level";
                else
                    type = "ALL";
                String text = automate.getIp() + " " + automate.getName() + " " +
                        automate.getRack() + " " + automate.getSlot() + " DB" + automate.getDatabloc()  + " " + automate.getDatabloc() + " " + type + " " +
                        automate.getType();
                if (text.toLowerCase().contains(s.toLowerCase())) {
                    searchList.add(automate);
                }
            }
        }
        notifyDataSetChanged();
    }

    public List<Automate> getData() {
        return this.searchList;
    }

    public void setData(List<Automate> automateList) {
        this.automateList = automateList;
        this.searchList = automateList;
    }

    class AutomateViewHolder extends RecyclerView.ViewHolder {

        private Automate automate;
        private TextView tv_rv_user_automate_main;
        private TextView tv_rv_user_automate_second;

        AutomateViewHolder(final View itemView) {
            super(itemView);

            resources = itemView.getContext().getResources();

            tv_rv_user_automate_main = itemView.findViewById(R.id.tv_rv_user_automate_main);
            tv_rv_user_automate_second = itemView.findViewById(R.id.tv_rv_user_automate_second);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    LayoutInflater layoutInflater = (LayoutInflater) itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    assert layoutInflater != null;
                    @SuppressLint("InflateParams") View dialogView = layoutInflater.inflate(R.layout.dialog_automate_add, null);
                    builder.setTitle(resources.getString(R.string.Edit));
                    builder.setView(dialogView);
                    final EditText et_dialog_automate_name = dialogView.findViewById(R.id.et_dialog_automate_name);
                    final EditText et_dialog_automate_ip = dialogView.findViewById(R.id.et_dialog_automate_ip);
                    final EditText et_dialog_automate_rack = dialogView.findViewById(R.id.et_dialog_automate_rack);
                    final EditText et_dialog_automate_slot = dialogView.findViewById(R.id.et_dialog_automate_slot);
                    final EditText et_dialog_automate_databloc = dialogView.findViewById(R.id.et_dialog_automate_databloc);
                    final Spinner sp_dialog_automate_type = dialogView.findViewById(R.id.sp_dialog_automate_type);
                    et_dialog_automate_name.setText(automate.getName());
                    et_dialog_automate_ip.setText(automate.getIp());
                    et_dialog_automate_rack.setText(String.valueOf(automate.getRack()));
                    et_dialog_automate_slot.setText(String.valueOf(automate.getSlot()));
                    et_dialog_automate_databloc.setText(String.valueOf(automate.getDatabloc()));
                    switch (automate.getType()) {
                        case 0:
                            sp_dialog_automate_type.setSelection(2);
                            break;
                        case 1:
                            sp_dialog_automate_type.setSelection(0);
                            break;
                        case 2:
                            sp_dialog_automate_type.setSelection(1);
                            break;
                    }
                    builder.setPositiveButton(R.string.Edit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String name = et_dialog_automate_name.getText().toString();
                            String ip = et_dialog_automate_ip.getText().toString();
                            String rack = et_dialog_automate_rack.getText().toString();
                            String slot = et_dialog_automate_slot.getText().toString();
                            String databloc = et_dialog_automate_databloc.getText().toString();
                            int type = -1;
                            String choosedType = sp_dialog_automate_type.getSelectedItem().toString();
                            String pillString = resources.getStringArray(R.array.arrayAutomateType)[0];
                            String levelString = resources.getStringArray(R.array.arrayAutomateType)[1];
                            String allString = resources.getStringArray(R.array.arrayAutomateType)[2];
                            if (choosedType.equals(pillString))
                                type = AutomateType.PILL;
                            else if (choosedType.equals(levelString))
                                type = AutomateType.LEVEL;
                            else if (choosedType.equals(allString))
                                type = AutomateType.ALL;
                            boolean error = false;
                            if (name.isEmpty()) {
                                et_dialog_automate_name.setError(resources.getString(R.string.errNameInvalid));
                                error = true;
                            }
                            if (!Patterns.IP_ADDRESS.matcher(ip).matches()) {
                                et_dialog_automate_ip.setError(resources.getString(R.string.errIPInvalid));
                                error = true;
                            }
                            if (!Regex.isDigit(rack)) {
                                et_dialog_automate_rack.setError(resources.getString(R.string.errRackInvalid));
                                error = true;
                            }
                            if (!Regex.isDigit(slot)) {
                                et_dialog_automate_slot.setError(resources.getString(R.string.errSlotInvalid));
                                error = true;
                            }
                            if (!Regex.isDigit(databloc)) {
                                et_dialog_automate_slot.setError(resources.getString(R.string.errDataBlocIncorrect));
                                error = true;
                            }
                            if (!error) {
                                List<Automate> list = automateList;
                                Automate newAutomate = new Automate(name, ip, Integer.parseInt(rack), Integer.parseInt(slot), Integer.parseInt(databloc), type);
                                list.set(list.indexOf(automate), newAutomate);
                                setData(list);
                                try {
                                    Gson gson = new Gson();
                                    FileOutputStream fos = itemView.getContext().openFileOutput("automates.json", Context.MODE_PRIVATE);
                                    String automateStr = gson.toJson(list);
                                    fos.write(automateStr.getBytes());
                                    fos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                notifyDataSetChanged();
                            }
                        }
                    });
                    builder.setCancelable(false);
                    builder.setNeutralButton(resources.getText(R.string.Delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String name = et_dialog_automate_name.getText().toString();
                            String ip = et_dialog_automate_ip.getText().toString();
                            String rack = et_dialog_automate_rack.getText().toString();
                            String slot = et_dialog_automate_slot.getText().toString();
                            String databloc = et_dialog_automate_databloc.getText().toString();
                            int type = 0;
                            switch (sp_dialog_automate_type.getSelectedItem().toString()) {
                                case "Pill":
                                    type = 1;
                                    break;
                                case "Level":
                                    type = 2;
                                    break;
                                case "ALL":
                                    type = 0;
                                    break;
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
                            if (!Regex.isDigit(rack)) {
                                et_dialog_automate_rack.setError(resources.getString(R.string.errRackInvalid));
                                error = true;
                            }
                            if (!Regex.isDigit(slot)) {
                                et_dialog_automate_slot.setError(resources.getString(R.string.errSlotInvalid));
                                error = true;
                            }
                            if (!Regex.isDigit(databloc)) {
                                et_dialog_automate_slot.setError(resources.getString(R.string.errDataBlocIncorrect));
                                error = true;
                            }
                            if (!error) {
                                automateList.remove(automate);
                                try {
                                    Gson gson = new Gson();
                                    FileOutputStream fos = itemView.getContext().openFileOutput("automates.json", Context.MODE_PRIVATE);
                                    String automateStr = gson.toJson(automateList);
                                    fos.write(automateStr.getBytes());
                                    fos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                notifyDataSetChanged();
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }

        void display(Automate automate) {
            this.automate = automate;
            tv_rv_user_automate_main.setText(automate.getName());
            String type = "";
            if (automate.getType() == AutomateType.PILL)
                type = "Pill";
            else if (automate.getType() == AutomateType.LEVEL)
                type = "Level";
            else if (automate.getType() == AutomateType.ALL)
                type = "ALL";
            String str = "IP: " + automate.getIp() + " Rack: " + automate.getRack() +
                    " Slot: " + automate.getSlot() + " DataBloc: DB" + automate.getDatabloc() + " Type: " + type;
            tv_rv_user_automate_second.setText(str);
        }

    }
}
