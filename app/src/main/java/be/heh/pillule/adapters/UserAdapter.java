package be.heh.pillule.adapters;

import android.content.res.Resources;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import be.heh.pillule.R;

/**
 * Created by gquittet on 12/13/17.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private String[][] datas;
    private int[] pictures;

    private Resources resources;

    public UserAdapter(int[] pictures, String[][] datas) {
        this.datas = datas;
        this.pictures = pictures;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.rv_user_menu, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        holder.display(pictures[position], datas[position]);
    }

    @Override
    public int getItemCount() {
        return datas.length;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        private ImageView iv_rv_user;
        private TextView tv_rv_user_title;
        private TextView tv_rv_user_content;
        private Button btn_rv_user;

        UserViewHolder(final View itemView) {
            super(itemView);

            resources = itemView.getContext().getResources();

            iv_rv_user = itemView.findViewById(R.id.iv_rv_user);
            tv_rv_user_title = itemView.findViewById(R.id.tv_rv_user_title);
            tv_rv_user_content = itemView.findViewById(R.id.tv_rv_user_content);
            btn_rv_user = itemView.findViewById(R.id.btn_rv_user);

        }

        void display(int picture, String[] data) {
            iv_rv_user.setImageDrawable(resources.getDrawable(picture));
            tv_rv_user_title.setText(data[0]);
            tv_rv_user_content.setText(data[1]);
            btn_rv_user.setText(data[2]);
        }

    }

}
