package com.github.informiguel.ComparaPrecios;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Build;
import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    LinearLayout itemLayout;
    Spinner unitTypeSpinner;
    UnidadesDeMedida unitWorker;

    // flags
    String currentCurrencySymbol;
    int currentRounding;
    boolean dontSaveDataFlag = false;
    private boolean userIsInteracting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        unitWorker = new UnidadesDeMedida(getApplicationContext());

        Settings.setUp(this);
        Settings.pullSettings();

        currentCurrencySymbol = Settings.currencySymbol;
        currentRounding = Settings.rounding;

        itemLayout = (LinearLayout) findViewById(R.id.itemLayout);
        unitTypeSpinner = (Spinner) findViewById(R.id.unitTypeSpnr);

        String[] items = unitWorker.tiposUnidades.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        unitTypeSpinner.setAdapter(adapter);

        ItemStorage is = new ItemStorage(this);
        ArrayList<ItemStructure> data = is.getData();

        if (data.size() > 0 && Settings.rememberData) {
            String unit = data.get(0).unit;
            for (int i = 0; i < unitWorker.unidadesMedida.size(); i++) {
                if (unitWorker.unidadesMedida.get(i).contains(unit)) {
                    unitTypeSpinner.setSelection(getSpinnerIndex(unitTypeSpinner, unitWorker.tiposUnidades.get(i)));
                }
            }

            for (ItemStructure item : data) {
                addItemManual(item.name, item.price, item.quantity, item.size, item.unit);
            }
        }

        ( (Spinner) findViewById(R.id.resultsUnitSpinner) ).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                generateResults();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        unitTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (userIsInteracting) {
                    if (getCurrentFocus() != null) {
                        getCurrentFocus().clearFocus();
                    }
                    unitTypeChanged(false);
                } else {
                    unitTypeChanged(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        findViewById(R.id.addItemLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem(view);
            }
        });

        findViewById(R.id.settingsLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettings(view);
            }
        });

        findViewById(R.id.clearBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearItems(view);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (dontSaveDataFlag) { return; }

        ArrayList<ItemStructure> data = new ArrayList<ItemStructure>();
        for (int i = 0; i < itemLayout.getChildCount(); i++) {
            ItemStructure item = new ItemStructure();
            LinearLayout itemTile = (LinearLayout) itemLayout.getChildAt(i);
            item.name = ( (EditText) itemTile.findViewById(R.id.nameEditText) ).getText().toString();
            item.price = ( (EditText) itemTile.findViewById(R.id.priceEditText) ).getText().toString().replace(Settings.currencySymbol, "");
            item.quantity = ( (EditText) itemTile.findViewById(R.id.quantityEditText) ).getText().toString();
            item.size = ( (EditText) itemTile.findViewById(R.id.sizePerQtyEditText) ).getText().toString();
            item.unit = ((Spinner) itemTile.findViewById(R.id.unitSpnr)).getSelectedItem().toString();
            data.add(item);
        }

        ItemStorage is = new ItemStorage(this);
        is.insertData(data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!currentCurrencySymbol.equals(Settings.currencySymbol)) {
            dontSaveDataFlag = true;
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
                // Usa la API moderna (requiere un Bundle)
                overrideActivityTransition(
                        Activity.OVERRIDE_TRANSITION_CLOSE,
                        0, // Animación de entrada
                        0  // Animación de salida
                );
            } else {
                // Usa la API obsoleta para dispositivos más antiguos
                // noinspection deprecation
                overridePendingTransition(0, 0);
            }
            startActivity(intent);
            return;
        }

        if (Settings.showResultsTile) {
            findViewById(R.id.resultsRoot).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.resultsRoot).setVisibility(View.GONE);
        }

        if (currentRounding != Settings.rounding) {
            currentRounding = Settings.rounding;
            for (int i = 0; i < itemLayout.getChildCount(); i++) {
                itemModified(itemLayout.getChildAt(i));
            }
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }

    protected void addItem(View view) {
        addItemManual("", "", "1", "", "");
    }

    protected void addItemManual(String name, String price, String quantity, String size, String unit) {
        // Inflate and get object
        View inflatedView = View.inflate(this, R.layout.item_tile, itemLayout);
        final LinearLayout recentlyAdded = (LinearLayout) itemLayout.getChildAt(itemLayout.getChildCount() - 1);
        final EditText nameEditText = (EditText) recentlyAdded.findViewById(R.id.nameEditText);
        final Spinner unitSpinner = (Spinner) recentlyAdded.findViewById(R.id.unitSpnr);
        final EditText priceEditText = (EditText) recentlyAdded.findViewById(R.id.priceEditText);
        final EditText quantityEditText = (EditText) recentlyAdded.findViewById(R.id.quantityEditText);
        final EditText sizePerQtyEditText = (EditText) recentlyAdded.findViewById(R.id.sizePerQtyEditText);

        // Set item name
        ((TextView) recentlyAdded.findViewById(R.id.nameEditText)).setText("Item " + itemLayout.getChildCount());

        // Link delete
        ImageButton deleteBtn = (ImageButton) recentlyAdded.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(v);
            }
        });

        // Link move up
        ImageButton moveUpBtn = (ImageButton) recentlyAdded.findViewById(R.id.moveUpBtn);
        moveUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveItemUp(v);
            }
        });

        // Link move down
        ImageButton moveDownBtn = (ImageButton) recentlyAdded.findViewById(R.id.moveDownBtn);
        moveDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveItemDown(v);
            }
        });

        // Set unit options
        setUnitOptions(recentlyAdded);
        unitSpinner.setSelection(getSpinnerIndex(unitSpinner, unit));

        // Whenever a the price is modified, re-calculate
        TextWatcher modificationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                itemModified(recentlyAdded);
            }
        };
        priceEditText.addTextChangedListener(modificationWatcher);

        // When selecting the price, remove the currency symbol. Show onblur
        priceEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            EditText editText = priceEditText;

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    String value = editText.getText().toString().replace(Settings.currencySymbol, "");
                    editText.setText(value, TextView.BufferType.EDITABLE);
                } else {
                    String value = editText.getText().toString();
                    if (!value.equals("")) {
                        editText.setText(value + Settings.currencySymbol, TextView.BufferType.EDITABLE);
                    }
                }
            }
        });

        // When the quantity is modified, re-calculate
        modificationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                itemModified(recentlyAdded);
            }
        };
        quantityEditText.addTextChangedListener(modificationWatcher);

        // When the size is modified, re-calculate
        modificationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                itemModified(recentlyAdded);
            }
        };
        sizePerQtyEditText.addTextChangedListener(modificationWatcher);

        // When setting the size, remove the units. Show onblur
        sizePerQtyEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            EditText editText = sizePerQtyEditText;
            Spinner unit_spinner = unitSpinner;

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    String unit = unit_spinner.getSelectedItem().toString();
                    String value = editText.getText().toString().replace(unit, "");
                    editText.setText(value, TextView.BufferType.EDITABLE);
                } else {
                    String value = editText.getText().toString();
                    if (!value.equals("")) {
                        String unit = unit_spinner.getSelectedItem().toString();
                        editText.setText(value + unit, TextView.BufferType.EDITABLE);
                    }
                }
            }
        });

        // Setup a watcher for the change of unit
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemModified(recentlyAdded);

                if (getCurrentFocus() != null) {
                    getCurrentFocus().clearFocus();
                }

                if (!"".equals(sizePerQtyEditText.getText().toString())) {
                    String currentValue = sizePerQtyEditText.getText().toString();
                    String value = currentValue.replaceAll("[^0-9.,]", "");
                    String unit = unitSpinner.getSelectedItem().toString();
                    sizePerQtyEditText.setText(value + unit);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Set passed values at the end to trigger everything
        if (name.compareTo("") != 0) {
            nameEditText.setText(name);
        }
        if (price.compareTo("") != 0) {
            // Add the price provided and prepend the currency symbol
            priceEditText.setText(price + Settings.currencySymbol);
        }
        if (quantity.compareTo("") != 0) {
            quantityEditText.setText(quantity);
        }
        if (size.compareTo("") != 0) {
            // Add the size provided and append the unit
            sizePerQtyEditText.setText(size + unitSpinner.getSelectedItem().toString());
        }

        // Setup watcher for name change
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                generateResults();
            }
        });
    }

    protected void setUnitOptions(View view) {
        // Set unit options for specific item (LinearLayout root)
        String unitType = unitTypeSpinner.getSelectedItem().toString();
        Spinner unitSpinner = (Spinner) view.findViewById(R.id.unitSpnr);

        int index = unitWorker.tiposUnidades.indexOf(unitType);
        String[] items = unitWorker.unidadesMedida.get(index).toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        unitSpinner.setAdapter(adapter);
    }

    protected void itemModified(View view) {
        // When an item/unit is modified, recalculate
        LinearLayout itemTile = (LinearLayout) view;
        

        if (getBasedItemValueValid(itemTile)) {
            //Si hay datos correctos para hacer los cálculos
            Double baseUnitValue1 = getBasedItemValue1(itemTile); // (3kg/€)
            Double baseUnitValue2 = getBasedItemValue2(itemTile); // (15€/kg)
            String unit = ((Spinner) itemTile.findViewById(R.id.unitSpnr)).getSelectedItem().toString();

                        ((TextView) itemTile.findViewById(R.id.ratiotextView)).setText(
                    // PRIMERA CONVERSIÓN: Precio por unidad (Ej: 15€/kg)
                        roundToString(unitWorker.convert2(unitWorker.getBaseUnit(unit), unit, baseUnitValue2))
                            + Settings.currencySymbol + "/" + unit

                            // DIVISOR
                            + " <--> "

                            // SEGUNDA CONVERSIÓN: Unidad por precio (Ej: 0.07kg/€)
                            + roundToString(unitWorker.convert(unitWorker.getBaseUnit(unit), unit, baseUnitValue1))
                            + unit + "/" + Settings.currencySymbol
            );
        } else {
            //si no hay datos suficientes solo se ve la moneda y unidad (€/kg)
            String unit = ((Spinner) itemTile.findViewById(R.id.unitSpnr)).getSelectedItem().toString();
            ((TextView) itemTile.findViewById(R.id.ratiotextView)).setText(Settings.currencySymbol + "/" + unit);
        }

        generateResults();
    }

    protected void moveItemUp(View view) {
        // Moves an item up. View passed in is the move up button.
        LinearLayout itemBeingMoved = (LinearLayout) view.getParent().getParent().getParent();
        int index = itemLayout.indexOfChild(itemBeingMoved);
        itemLayout.removeView(itemBeingMoved);
        itemLayout.addView(itemBeingMoved, Math.max(index - 1, 0)); // Make sure we don't put an item in -1
    }

    protected void moveItemDown(View view) {
        // Moves an item down. View passed in is the move down button.
        LinearLayout itemBeingMoved = (LinearLayout) view.getParent().getParent().getParent();
        int index = itemLayout.indexOfChild(itemBeingMoved);
        itemLayout.removeView(itemBeingMoved);
        itemLayout.addView(itemBeingMoved, Math.min(index + 1, itemLayout.getChildCount()));
    }

    protected void deleteItem(View view) {
        // Removes an item. View passed in is the delete button.
        itemLayout.removeView((LinearLayout) view.getParent().getParent().getParent());
        generateResults();
    }

    protected void clearItems(View view) {
        // Clear all items in the itemLayout
        if  (itemLayout.getChildCount() > 0) {
            itemLayout.removeAllViews();
        }
        generateResults();
    }

    protected void unitTypeChanged(boolean initial) {
        // When the main unit type is changed, assign new units to items
        if (!initial) {
            for (int i = 0; i < itemLayout.getChildCount(); i++) {
                setUnitOptions(itemLayout.getChildAt(i));
            }
        }

        String unitType = unitTypeSpinner.getSelectedItem().toString();
        int index = unitWorker.tiposUnidades.indexOf(unitType);
        String[] items = unitWorker.unidadesMedida.get(index).toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.results_spinner_item, items);
        ( (Spinner) findViewById(R.id.resultsUnitSpinner) ).setAdapter(adapter);

    }

    protected void generateResults() {
        // Recalculate the results tile
        // Muestra los resultados ordenados de mejor a peor
        LinearLayout results = (LinearLayout) findViewById(R.id.results);
        results.removeAllViews();

        if (( (Spinner) findViewById(R.id.resultsUnitSpinner) ).getCount() < 1) {
            return;
        }

        String requestedUnit = ( (Spinner) findViewById(R.id.resultsUnitSpinner) ).getSelectedItem().toString();
        String baseUnit = unitWorker.getBaseUnit(requestedUnit);
        LinearLayout recentlyAdded;

        ArrayList<LinearLayout> itemTiles = new ArrayList<>();  //lista con nombres de productos
        for (int i = 0; i < itemLayout.getChildCount(); i++) { //relllena la lista
            if (getBasedItemValueValid(itemLayout.getChildAt(i))) {
                itemTiles.add((LinearLayout) itemLayout.getChildAt(i));
            }
        }

        Double bestValue1, bestValue2;  // la mayor cantidad por 1 moneda y el mayor precio por unidad
        int bestValueIndex;  //lugar en la lista donde está la mayor cantidad por 1 moneda
        // se va a ordenar de mejor a peor: de menor precio y de más cantidad por moneda
        while (itemTiles.size() > 0) {
            bestValue1 = getBasedItemValue1(itemTiles.get(0));  //primer elemento de la lista
            bestValue2 = getBasedItemValue2(itemTiles.get(0));
            bestValueIndex = 0;
            for (int i = 1; i < itemTiles.size(); i++) { //busca el de mayor cantidad de la lista
                if (getBasedItemValue1(itemTiles.get(i)) > bestValue1) {
                    bestValue1 = getBasedItemValue1(itemTiles.get(i)); //más cantidad
                    bestValue2 = getBasedItemValue2(itemTiles.get(i)); //menor precio
                    bestValueIndex = i;
                }
            }
            View inflatedView = View.inflate(this, R.layout.results_item, results);
            recentlyAdded = (LinearLayout) results.getChildAt(results.getChildCount() - 1);
            //nombre del elemento
            ( (TextView) recentlyAdded.findViewById(R.id.resultItemName) ).setText(( (EditText) itemTiles.get(bestValueIndex).findViewById(R.id.nameEditText) ).getText().toString() + ": ") ;

            //menor precio (monedas por unidad de cantidad)
            ( (TextView) recentlyAdded.findViewById(R.id.resultItemValue1) ).setText(roundToString(unitWorker.convert2(baseUnit, requestedUnit, bestValue2)) + Settings.currencySymbol + "/" + requestedUnit + " <-->");
            //más cantidad (cantidad por unidad de moneda)
            ( (TextView) recentlyAdded.findViewById(R.id.resultItemValue2) ).setText(roundToString(unitWorker.convert(baseUnit, requestedUnit, bestValue1)) + requestedUnit + "/" + Settings.currencySymbol);

            itemTiles.remove(bestValueIndex); //borra de la lista el elemento ya procesado
        }
    }

    private boolean getBasedItemValueValid(View view) {
        LinearLayout itemTile = (LinearLayout) view;
        String priceText = ( (EditText) itemTile.findViewById(R.id.priceEditText) ).getText().toString().replace(Settings.currencySymbol, "");
        String quantityText = ( (EditText) itemTile.findViewById(R.id.quantityEditText) ).getText().toString();
        String amountPerQtyText = ( (EditText) itemTile.findViewById(R.id.sizePerQtyEditText) ).getText().toString().replaceAll("[^0-9.,]", "");

        try {
            Double.parseDouble(priceText);
            Double.parseDouble(quantityText);
            Double.parseDouble(amountPerQtyText);
        } catch (NumberFormatException e){
            return false;
        }

        return priceText.compareTo("") != 0 && quantityText.compareTo("") != 0 && amountPerQtyText.compareTo("") != 0;
    }

    private Double getBasedItemValue1(View view) {
        //calcula la cantidad recibida por una moneda (3kg/€)
        if (!getBasedItemValueValid(view)) {
            return 0.0;
        }
        LinearLayout itemTile = (LinearLayout) view;
        String priceText = ( (EditText) itemTile.findViewById(R.id.priceEditText) ).getText().toString().replace(Settings.currencySymbol, "");
        String quantityText = ( (EditText) itemTile.findViewById(R.id.quantityEditText) ).getText().toString();
        String amountPerQtyText = ( (EditText) itemTile.findViewById(R.id.sizePerQtyEditText) ).getText().toString().replaceAll("[^0-9.,]", "");

        Double precio = Double.parseDouble(priceText);
        Double cantidad = Double.parseDouble(quantityText);
        Double unidades = Double.parseDouble(amountPerQtyText);
        Double unidadesPorEuro = (cantidad * unidades) / precio;
        String unit = ((Spinner) itemTile.findViewById(R.id.unitSpnr)).getSelectedItem().toString();

        return unitWorker.convertToBase(unit, unidadesPorEuro);
    }

       private Double getBasedItemValue2(View view) {
        //calcula el dinero necesario por una medida (15€/kg)
        if (!getBasedItemValueValid(view)) {
            return 0.0;
        }
        LinearLayout itemTile = (LinearLayout) view;
        String priceText = ( (EditText) itemTile.findViewById(R.id.priceEditText) ).getText().toString().replace(Settings.currencySymbol, "");
        String quantityText = ( (EditText) itemTile.findViewById(R.id.quantityEditText) ).getText().toString();
        String amountPerQtyText = ( (EditText) itemTile.findViewById(R.id.sizePerQtyEditText) ).getText().toString().replaceAll("[^0-9.,]", "");

        Double precio = Double.parseDouble(priceText);
        Double cantidad = Double.parseDouble(quantityText);
        Double unidades = Double.parseDouble(amountPerQtyText);
        Double convertido = cantidad * unidades;
        Double EurosPorUnidad = precio / convertido;
        String unit = ((Spinner) itemTile.findViewById(R.id.unitSpnr)).getSelectedItem().toString();

        return unitWorker.convertRateToBase(unit, EurosPorUnidad);
    }
    private String roundToString(Double value) {
        StringBuilder format = new StringBuilder("#.#");
        for (int i = 0; i < Settings.rounding - 1; i++) {
            format.append("#");
        }

        DecimalFormat df = new DecimalFormat(format.toString());
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(value);
    }

    private int getSpinnerIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }

        return 0;
    }

    protected void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}
