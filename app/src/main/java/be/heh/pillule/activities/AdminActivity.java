package be.heh.pillule.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import be.heh.pillule.R;
import be.heh.pillule.adapters.AdminAdapter;
import be.heh.pillule.database.UserAccess;
import be.heh.pillule.database.UserRepository;

public class AdminActivity extends Activity implements SearchView.OnQueryTextListener {

    private Resources resources;

    private RecyclerView rv_admin_usersList;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SearchView searchView;

    private UserRepository userRepository;

    private final int ACTIVITY_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        resources = getApplicationContext().getResources();

        rv_admin_usersList = findViewById(R.id.rv_admin_usersList);
        rv_admin_usersList.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        rv_admin_usersList.setLayoutManager(mLayoutManager);

        UserAccess userDb = new UserAccess(this);
        userDb.openForRead();
        userRepository = new UserRepository(userDb.getDb());
        mAdapter = new AdminAdapter(userRepository);
        rv_admin_usersList.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.ab_admin_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        ((AdminAdapter) mAdapter).updateDatas(userRepository.getAll());
                        ((AdminAdapter) mAdapter).filter(searchView.getQuery().toString());
                        break;
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ab_admin_search:
                return true;
            case R.id.ab_admin_disconnect:
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

    @Override
    public void onBackPressed() {
        logout();
        moveTaskToBack(true);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        ((AdminAdapter) mAdapter).filter(s);
        return false;
    }
}
