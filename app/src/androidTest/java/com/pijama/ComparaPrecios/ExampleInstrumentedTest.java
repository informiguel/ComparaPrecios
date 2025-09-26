package com.pijama.ComparaPrecios;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry; // Changed from android.support.test
import androidx.test.ext.junit.runners.AndroidJUnit4;     // Changed from android.support.test.runner

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */


@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.pijama.ComparaPrecios", appContext.getPackageName());
    }
}
