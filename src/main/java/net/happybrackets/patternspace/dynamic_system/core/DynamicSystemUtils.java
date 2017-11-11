package net.happybrackets.patternspace.dynamic_system.core;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.happybrackets.patternspace.mapping.MapTools;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class DynamicSystemUtils {

    public static DynamicSystem readFromJSON(Reader in) throws ClassNotFoundException, IOException, InvocationTargetException, IllegalAccessException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(in);
        JsonParser parser = new JsonParser();
        JsonArray array = (JsonArray)parser.parse(reader);
        JsonObject obj = (JsonObject)array.get(0);
        String className = obj.get("class").getAsString();
        Class<? extends DynamicSystem> clazz = null;
        clazz = (Class<? extends DynamicSystem>) Class.forName(className);
        DynamicSystem result = null;
        Method readJSONMethod = null;
        try {
            readJSONMethod = clazz.getMethod("readJSON", JsonElement.class);
        } catch (Exception e) {
            readJSONMethod = null;
        }
        if(readJSONMethod != null) {
            result = (DynamicSystem) readJSONMethod.invoke(null, (JsonObject) array.get(1));
            System.out.println("Read " + clazz.getName() + " from readJSON method");
        } else {
            result = gson.fromJson((JsonObject) array.get(1), clazz);
            System.out.println("Read " + clazz.getName() + " via gson");
        }
        return result;
    }

    public static void writeToJSON(DynamicSystem object, Writer out) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject header = new JsonObject();
        header.addProperty("class", object.getClass().getName());
        JsonArray array = new JsonArray();
        array.add(header);
        array.add(object.writeJSON());

        gson.toJson(array, out);
    }

    public static Number[] getOutputs(Number[] outputs, Class<? extends Number> typeFilter) {
        List<Number> filteredOutputs = new ArrayList<>();
        int filteredObjectIndex = 0;
        for(int i = 0; i < outputs.length; i++) {
            if(outputs[i].getClass().equals(typeFilter)) {
                filteredOutputs.add(outputs[i]);
            }
        }
        return filteredOutputs.toArray(new Number[]{});
    }

    public static double distance(double[] x, double[] y) {
        double distance = 0;
        for(int i = 0; i < x.length; i++) {
            distance += (x[i] - y[i]) * (x[i] - y[i]);
        }
        return Math.sqrt(distance);
    }

}
