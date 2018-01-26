package be.heh.pillule.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import be.heh.pillule.R;
import be.heh.pillule.activities.EditUserActivity;
import be.heh.pillule.database.User;
import be.heh.pillule.database.UserRepository;
import be.heh.pillule.security.Admin;

/**
 * Created by gquittet on 12/10/17.
 */

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {

    private ArrayList<User> users;
    private UserRepository userRepository;
    Resources resources;

    public AdminAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.users = userRepository.getAll();
    }

    @Override
    public AdminViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.rv_admin_user, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdminViewHolder holder, int position) {
        User user = users.get(position);
        holder.display(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void filter(String s) {
        if (s.length() == 0) {
            users = userRepository.getAll();
        } else {
            users = new ArrayList<>();
            for (User user : userRepository.getAll()) {
                String role = user.getRoles() == 0 ? resources.getString(R.string.Read) :
                        resources.getString(R.string.Write);
                String text;
                if (user.getEmail().equals(Admin.getUsername())) {
                    text = user.getId() + " " + user.getEmail() + " " +
                            resources.getString(R.string.Administrator);
                } else {
                    text = user.getId() + " " + user.getLastname() + " " + user.getFirstname() + " " +
                            user.getEmail() + " " + role + " " + user.getRoles();
                }
                if (text.toLowerCase().contains(s.toLowerCase())) {
                    users.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateDatas(ArrayList<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    private void removeAt(int position, User user) {
        userRepository.remove(user.getEmail());
        users.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeRemoved(position, users.size());
    }

    class AdminViewHolder extends RecyclerView.ViewHolder {

        TextView tv_rv_admin_main;
        TextView tv_rv_admin_second;
        User user;

        AdminViewHolder(final View itemView) {
            super(itemView);
            tv_rv_admin_main = itemView.findViewById(R.id.tv_rv_admin_main);
            tv_rv_admin_second = itemView.findViewById(R.id.tv_rv_admin_second);

            resources = itemView.getContext().getResources();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String role = user.getRoles() == 0 ? resources.getString(R.string.Read) :
                            resources.getString(R.string.Write);
                    String message;
                    if (user.getEmail().equals(Admin.getUsername())) {
                        message = "<b><i>ID</i></b>&nbsp;&nbsp;" + user.getId() + "<br>" +
                                "<b><i>" + resources.getString(R.string.Email) + "</i></b><br>" + user.getEmail();
                        new AlertDialog.Builder(itemView.getContext())
                                .setTitle(user.getEmail())
                                .setMessage(Html.fromHtml(message))
                                .setPositiveButton(R.string.Close, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                }).setNegativeButton(R.string.Edit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(itemView.getContext(), EditUserActivity.class);
                                intent.putExtra("id", user.getId());
                                ((Activity) itemView.getContext()).startActivityForResult(intent, 1);
                            }
                        }).show();
                    } else {
                        message = "<b><i>ID</i></b>&nbsp;&nbsp;" + user.getId() + "<br>" +
                                "<b><i>" + resources.getString(R.string.Lastname) + "</i></b><br>" + user.getLastname() + "<br>" +
                                "<b><i>" + resources.getString(R.string.Firstname) + "</i></b><br>" + user.getFirstname() + "<br>" +
                                "<b><i>" + resources.getString(R.string.Email) + "</i></b><br>" + user.getEmail() + "<br>" +
                                "<b><i>" + resources.getString(R.string.Role) + "</i></b>&nbsp;&nbsp;" + role;
                        new AlertDialog.Builder(itemView.getContext())
                                .setTitle(user.getEmail())
                                .setMessage(Html.fromHtml(message))
                                .setPositiveButton(R.string.Close, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                }).setNegativeButton(R.string.Edit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(itemView.getContext(), EditUserActivity.class);
                                intent.putExtra("id", user.getId());
                                ((Activity) itemView.getContext()).startActivityForResult(intent, 1);
                            }
                        }).setNeutralButton(R.string.Delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int userPosition = users.indexOf(user);
                                removeAt(userPosition, user);
                            }
                        }).show();
                    }
                }
            });
        }

        void display(User user) {
            this.user = user;
            tv_rv_admin_main.setText(user.getEmail());
            String role = user.getRoles() == 0 ? resources.getString(R.string.Read) :
                    resources.getString(R.string.Write);
            String infos = resources.getString(R.string.Administrator);
            if (!user.getEmail().equals(Admin.getUsername())) {
                infos = user.getLastname() + " " + user.getFirstname() + " " + user.getEmail() +
                        " " + role;
            }
            tv_rv_admin_second.setText(infos);
        }
    }
}
