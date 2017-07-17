package com.trackexpense.snapbill;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.pixplicity.easyprefs.library.Prefs;
import com.trackexpense.snapbill.activity.AllBillsActivity;
import com.trackexpense.snapbill.activity.NewBillActivity;
import com.trackexpense.snapbill.utils.Constants;
import com.trackexpense.snapbill.utils.GUIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.app_name_left)
    TextView appNameLeft;
    @BindView(R.id.app_name_right)
    TextView appNameRight;

    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;

    TextView userEmailTextView;
    TextView userNameTextView;

    private static final String TAG = MainActivity.class.getSimpleName();

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("");
        ButterKnife.bind(this);

        this.context = getApplicationContext();

        initLayout();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    private void initLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                userNameTextView.setSelected(true);
                userEmailTextView.setSelected(true);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        View headerLayout = navigationView.getHeaderView(0);

        userNameTextView = GUIUtils.getView(headerLayout,R.id.user_name);
        userNameTextView.setText(Prefs.getString(Constants.PREF_NAME,"Name"));
        userEmailTextView = GUIUtils.getView(headerLayout,R.id.user_email);
        userEmailTextView.setText(Prefs.getString(Constants.PREF_EMAIL,"name@domain.com"));
    }

    @OnClick(R.id.new_bill_button)
    protected void onNewBillClick() {
        Intent intent = new Intent(this, NewBillActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        appNameLeft.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,
//                R.anim.slide_in_from_left));
//        Animation slideInFromRightAnimation = AnimationUtils.loadAnimation(MainActivity.this,
//                R.anim.slide_in_from_right);
//        slideInFromRightAnimation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
////                fab.show(true);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//        });
//        appNameRight.startAnimation(slideInFromRightAnimation);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_my_bills:
                Intent allBillsIntent = new Intent(this, AllBillsActivity.class);
                startActivity(allBillsIntent);
                break;
            case R.id.nav_logout:
                Config.showToastMessage(MainActivity.this,"logout");
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
