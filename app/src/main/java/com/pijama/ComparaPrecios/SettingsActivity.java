package com.pijama.ComparaPrecios;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebViewClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ajustes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.settings_and_about));

        final EditText currencyValueTextEdit = findViewById(R.id.currencyValueTextEdit);
        final Spinner roundingValueSpinner = findViewById(R.id.roundingValueSpinner);
        final Switch showResultsValueSwitch = findViewById(R.id.showResultsValueSwitch);
        final Switch rememberDataValueSwitch = findViewById(R.id.rememberDataValueSwitch);
        final TextView versionTag = findViewById(R.id.versionTag);

        String[] decimales = new String[] {"1", "2", "3", "4", "5", "6"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, decimales);
        roundingValueSpinner.setAdapter(adapter);

        currencyValueTextEdit.setText(Settings.currencySymbol);
        roundingValueSpinner.setSelection(adapter.getPosition(String.valueOf(Settings.rounding)));
        showResultsValueSwitch.setChecked(Settings.showResultsTile);
        rememberDataValueSwitch.setChecked(Settings.rememberData);


        versionTag.setText(getString(R.string.version, BuildConfig.VERSION_NAME));

        currencyValueTextEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Settings.currencySymbol = currencyValueTextEdit.getText().toString();
                Settings.pushSettings();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        }); // CIERRE de la CLASE anónima.CIERRE de los ARGUMENTOS. FIN de la SENTENCIA.

        roundingValueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Settings.rounding = Integer.parseInt(roundingValueSpinner.getSelectedItem().toString());
                Settings.pushSettings();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        showResultsValueSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.showResultsTile = showResultsValueSwitch.isChecked();
                Settings.pushSettings();
            }
        });

        rememberDataValueSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.rememberData = rememberDataValueSwitch.isChecked();
                Settings.pushSettings();
            }
        });


        findViewById(R.id.bugReportLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bugReport(view);
            }
        });

        findViewById(R.id.ayudaLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ayuda(view);
            }
        });

        findViewById(R.id.developerSiteLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                developerSite(view);
            }
        });

        findViewById(R.id.licensesLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                licenses(view);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Settings.pushSettings();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    protected void bugReport(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.bug_reporting_url)));
        startActivity(browserIntent);
    }
    protected void ayuda(View view) {
        WebView webView = (WebView) LayoutInflater.from(this).inflate(R.layout.ayuda, null);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Devolver false indica que la WebView debe cargar la URL internamente.
                return false;
            }
        });

        // 1. Obtener el código de idioma actual (sin cambios)
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        String languageCode = config.getLocales().get(0).getLanguage();

        // 2. Definir la URL base del fichero con la ayuda
        String baseUrl = "file:///android_asset/ayuda_";
        String url;

        // 3. Elegir idioma. Comprobar si tenemos un archivo para ese idioma
        if (languageCode.equals("es")) {
            url = baseUrl + "es.html";
        } else if (languageCode.equals("pt")) {
            url = baseUrl + "pt.html";
        } else if (languageCode.equals("br")) {
            url = baseUrl + "br.html";
        } else if (languageCode.equals("en")) {
            url = baseUrl + "en.html";
        } else if (languageCode.equals("nl")) {
            url = baseUrl + "nl.html";
        }
        // ... AGREGAR OTROS IDIOMAS ... Por defecto ES
        else {
            url = baseUrl + "es.html"; // Opción de reserva
        }
        // 4. Cargar la URL dinámica
        webView.loadUrl(url);

        // 5. Muestra el AlertDialog
        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(this);
        mAlertDialog.setTitle(getString(R.string.ayuda));
        mAlertDialog.setView(webView);
        mAlertDialog.setPositiveButton(android.R.string.ok, null);
        mAlertDialog.show();
    }

    protected void developerSite(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.developer_site_url)));
        startActivity(browserIntent);
    }

    protected void licenses(View view) {
        WebView webView = (WebView) LayoutInflater.from(this).inflate(R.layout.dialog_licenses, null);
        webView.loadData(getString(R.string.licenses_html), "text/html", null);
        //AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(this);
        mAlertDialog.setTitle("Licencia");
        mAlertDialog.setView(webView);
        mAlertDialog.setPositiveButton(android.R.string.ok, null);
        mAlertDialog.show();
    }
}
