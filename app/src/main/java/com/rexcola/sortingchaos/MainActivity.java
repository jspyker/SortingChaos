package com.rexcola.sortingchaos;

        import android.app.Activity;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;

public class MainActivity extends Activity {

    private static final int MENU_RESET = 1;

    private static final int MENU_STOP = 2;

    private SortingChaosView sortingChaosView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_RESET, 0, R.string.menu_reset);
        menu.add(0, MENU_STOP, 0, R.string.menu_stop);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                sortingChaosView.getThread().doReset();
                return true;
            case MENU_STOP:
            	finish();
                return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // tell system to use the layout defined in our XML file
        setContentView(R.layout.activity_main);

        // get handles to the CatchView from XML, and its thread
        sortingChaosView = findViewById(R.id.sortingChaosView);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // just have the View save its state into our Bundle
        super.onSaveInstanceState(outState);
    }
}
