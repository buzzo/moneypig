package br.com.buzzo.moneypig.test;

import br.com.buzzo.moneypig.Main;
import android.app.TabActivity;
import android.test.ActivityInstrumentationTestCase2;

public class MainTest extends ActivityInstrumentationTestCase2<Main> {
    
    private TabActivity main;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        this.main = getActivity();
    }

    
    
    public MainTest(String pkg, Class<Main> activityClass) {
        super("br.com.buzzo.moneypig", Main.class);
    }

}
