package com.pijama.ComparaPrecios;

import android.content.Context;

import java.util.ArrayList;

public class UnidadesDeMedida {

    public ArrayList<String> tiposUnidades = new ArrayList<>();
    public ArrayList<ArrayList<String>> unidadesMedida = new ArrayList<>();
    private ArrayList<ArrayList<Double>> unidadBaseRelativa = new ArrayList<>();

    public UnidadesDeMedida(Context context) {
        tiposUnidades.add(context.getResources().getString(R.string.weight));
        tiposUnidades.add(context.getResources().getString(R.string.volume));
        tiposUnidades.add(context.getResources().getString(R.string.length));
        tiposUnidades.add(context.getResources().getString(R.string.pieces));

        ArrayList<String> units_weight = new ArrayList<>();
        ArrayList<Double> units_weight_relativity = new ArrayList<>();
        units_weight.add("g");
        units_weight_relativity.add(1.0);
        units_weight.add("kg");
        units_weight_relativity.add(0.001);
        units_weight.add("kg");
        units_weight_relativity.add(0.001);
        units_weight.add("t"); // toneladas
        units_weight_relativity.add(0.000001);
        units_weight.add("oz"); // Ounce
        units_weight_relativity.add(0.035274);
        units_weight.add("lb"); // Pound
        units_weight_relativity.add(0.0022046249999752);
        units_weight.add("st"); // Stone
        units_weight_relativity.add(0.00015747321428394284561);
        unidadesMedida.add(units_weight); // Match the first index as "Weight" is in unitTypes
        unidadBaseRelativa.add(units_weight_relativity);

        ArrayList<String> units_volume = new ArrayList<>();
        ArrayList<Double> units_volume_relativity = new ArrayList<>();
        units_volume.add("l");
        units_volume_relativity.add(1.0);
        units_volume.add("cl");
        units_volume_relativity.add(100.0);
        units_volume.add("ml");
        units_volume_relativity.add(1000.0);
        units_volume.add("fl oz (US)"); // fluid oz (US)
        units_volume_relativity.add(33.814042178720001175);
        units_volume.add("pt (US)"); // pint (US)
        units_volume_relativity.add(2.1133776361700000734);
        units_volume.add("gal (US)"); // gallon (US)
        units_volume_relativity.add(0.26417220452125);
        units_volume.add("fl oz (Imp)"); // fluid oz (Imperial)
        units_volume_relativity.add(35.195100000219895264);
        units_volume.add("pt (Imp)"); // pint (Imperial)
        units_volume_relativity.add(2.0000011519999958409);
        units_volume.add("gal (Imp)"); // gallon (Imperial)
        units_volume_relativity.add(0.21996937500137433985);
        unidadesMedida.add(units_volume);
        unidadBaseRelativa.add(units_volume_relativity);

        ArrayList<String> units_length = new ArrayList<>();
        ArrayList<Double> units_length_relativity = new ArrayList<>();
        units_length.add("m");
        units_length_relativity.add(1.0);
        units_length.add("mm");
        units_length_relativity.add(1000.0);
        units_length.add("cm");
        units_length_relativity.add(100.0);
        units_length.add("km");
        units_length_relativity.add(0.001);
        units_length.add("in"); // inch
        units_length_relativity.add(39.3701);
        units_length.add("yd"); // yard
        units_length_relativity.add(1.093613888889);
        units_length.add("mi"); // mile
        units_length_relativity.add(0.00062137152777784086452);
        unidadesMedida.add(units_length);
        unidadBaseRelativa.add(units_length_relativity);

        ArrayList<String> units_pieces = new ArrayList<>();
        ArrayList<Double> units_pieces_relativity = new ArrayList<>();
        units_pieces.add("pzas");
        units_pieces_relativity.add(1.0);
        unidadesMedida.add(units_pieces);
        unidadBaseRelativa.add(units_pieces_relativity);
    }

    public Double convert(String unitA, String unitB, Double value) {
        Double unitARelativity = 1.0;
        Double unitBRelativity = 1.0;
        for (ArrayList<String> unitGroup: unidadesMedida) {
            if (unitGroup.contains(unitA)) {
                // Get the item in unitTypes where the index matches the ArrayList we found the item in units
                int positionIndex = unidadesMedida.indexOf(unitGroup);
                int unitAIndex = unitGroup.indexOf(unitA);
                int unitBIndex = unitGroup.indexOf(unitB);
                unitARelativity = unidadBaseRelativa.get(positionIndex).get(unitAIndex);
                unitBRelativity = unidadBaseRelativa.get(positionIndex).get(unitBIndex);
            }
        }

        value = value / unitARelativity;
        value = value * unitBRelativity;

        return value;
    }


    public Double convertToBase(String unit, Double value) {
        for (ArrayList<String> unitGroup : unidadesMedida) {
            if (unitGroup.contains(unit)) {
                value = convert(unit, unitGroup.get(0),value);
            }
        }

        return value;
    }
    //Para convertir TARIFAS €/A a €/B
    public Double convert2(String unitA, String unitB, Double value) {
        Double unitARelativity = 1.0;
        Double unitBRelativity = 1.0;

        for (ArrayList<String> unitGroup: unidadesMedida) {
            if (unitGroup.contains(unitA) && unitGroup.contains(unitB)) {
                int positionIndex = unidadesMedida.indexOf(unitGroup);
                int unitAIndex = unitGroup.indexOf(unitA);
                int unitBIndex = unitGroup.indexOf(unitB);

                unitARelativity = unidadBaseRelativa.get(positionIndex).get(unitAIndex);
                unitBRelativity = unidadBaseRelativa.get(positionIndex).get(unitBIndex);
                // LÓGICA INVERSA para tarifas (€/A a €/B):
                // 1. Convertir de A a la Base: Multiplicar por el factor de A.
                value = value * unitARelativity;
                // 2. Convertir de la Base a B: Dividir por el factor de B.
                value = value / unitBRelativity;
                return value;
            }
        }
        return value;
    }
//Convierte una tarifa €/Unidad a €/Base. Necesaria para el cálculo del bestValue2
    public Double convertRateToBase(String unit, Double value) {
        for (ArrayList<String> unitGroup : unidadesMedida) {
            if (unitGroup.contains(unit)) {
                String baseUnit = unitGroup.get(0);
                // Usamos convert2 (la lógica inversa) para convertir la tarifa a la unidad base.
                // unit: unidad original del producto seleccionado (ej: "kg")
                // baseUnit: unidad base del grupo a la hora de ver mejor resultado (ej: "g")
                value = convert2(unit, baseUnit, value);
                return value;
            }
        }
        return value;
    }
    public String getBaseUnit(String unit) {
        String baseUnit = "";
        for (ArrayList<String> unitGroup : unidadesMedida) {
            if (unitGroup.contains(unit)) {
                baseUnit = unitGroup.get(0);
            }
        }
        return baseUnit;
    }
}
