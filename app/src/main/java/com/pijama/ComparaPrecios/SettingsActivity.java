package com.pijama.ComparaPrecios;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceRequest;
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

        String[] decimales = new String[]{"1", "2", "3", "4", "5", "6"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, decimales);
        roundingValueSpinner.setAdapter(adapter);

        currencyValueTextEdit.setText(Settings.currencySymbol);
        roundingValueSpinner.setSelection(adapter.getPosition(String.valueOf(Settings.rounding)));
        showResultsValueSwitch.setChecked(Settings.showResultsTile);
        rememberDataValueSwitch.setChecked(Settings.rememberData);
        versionTag.setText(getString(R.string.version, BuildConfig.VERSION_NAME));

        currencyValueTextEdit.addTextChangedListener(new SimpleTextWatcher(s -> {
            Settings.currencySymbol = s;
            Settings.pushSettings();
        }));

        roundingValueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Settings.rounding = Integer.parseInt(roundingValueSpinner.getSelectedItem().toString());
                Settings.pushSettings();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        showResultsValueSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Settings.showResultsTile = isChecked;
            Settings.pushSettings();
        });

        rememberDataValueSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Settings.rememberData = isChecked;
            Settings.pushSettings();
        });

        // Eventos de clic
        findViewById(R.id.bugReportLayout).setOnClickListener(this::bugReport);
        findViewById(R.id.ayudaLayout).setOnClickListener(this::ayuda);
        findViewById(R.id.developerSiteLayout).setOnClickListener(this::developerSite);
        findViewById(R.id.licensesLayout).setOnClickListener(this::licenses);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Settings.pushSettings();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ---------- Métodos de acción ----------

    protected void bugReport(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.bug_reporting_url))));
    }

    protected void ayuda(View view) {
        WebView webView = (WebView) LayoutInflater.from(this).inflate(R.layout.ayuda, null);

        // Configuración segura del WebView
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);

        // Cliente que abre los enlaces externos en navegador
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true; // manejar fuera de la WebView
                }
                return false; // dejar que la WebView maneje URLs locales
            }
        });

        // Determinar idioma y cargar el archivo correspondiente
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        String languageCode = config.getLocales().get(0).getLanguage();

        String baseUrl = "file:///android_asset/ayuda_";
        String url;

        switch (languageCode) {
            case "pt":
            case "br":
                url = baseUrl + "pt.html";
                break;
            case "en":
                url = baseUrl + "en.html";
                break;
            case "nl":
                url = baseUrl + "nl.html";
                break;
            case "ast":
                url = baseUrl + "ast.html";
                break;
            default:
                url = baseUrl + "es.html";
                break;
        }

        webView.loadUrl(url);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.ayuda))
                .setView(webView)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    protected void developerSite(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.developer_site_url))));
    }

    protected void licenses(View view) {
        WebView webView = (WebView) LayoutInflater.from(this).inflate(R.layout.dialog_licenses, null);
        webView.loadData(getString(R.string.licenses_html), "text/html", "utf-8");

        new AlertDialog.Builder(this)
                .setTitle("Licencia")
                .setView(webView)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    // ---------- Clase auxiliar ----------
    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final java.util.function.Consumer<String> consumer;
        SimpleTextWatcher(java.util.function.Consumer<String> consumer) {
            this.consumer = consumer;
        }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            consumer.accept(s.toString());
        }
        @Override public void afterTextChanged(android.text.Editable s) {}
    }
}
