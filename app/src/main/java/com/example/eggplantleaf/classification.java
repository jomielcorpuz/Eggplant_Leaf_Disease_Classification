package com.example.eggplantleaf;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.drawerlayout.widget.DrawerLayout;

public class classification extends Activity {
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classification);

        drawerLayout = findViewById(R.id.drawer_layout);
    }
    public void ClickMenu(View view){

        MainActivity.openDrawer(drawerLayout);
    }

    public void ClickLogo(View view){
        MainActivity.closeDrawer(drawerLayout);
    }
    public void ClickHome(View view){
        MainActivity.redirectActivity(this,MainActivity.class);
    }
    public void ClickClassification(View view){
        recreate();
    }
    public void ClickAbout(View view){ MainActivity.redirectActivity(this, about.class);
    }
    public void ClickExit(View view){
        MainActivity.exit(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        MainActivity.closeDrawer(drawerLayout);
    }
}
