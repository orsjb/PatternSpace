import net.happybrackets.patternspace.dynamic_system.core.DynamicSystem;
import net.happybrackets.patternspace.dynamic_system.decider.Decider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class TestDeciderJSON {

    public static void main(String[] args) {

        Random rng = new Random();

        String outputDir = "build/tmp/testOutput";
        new File(outputDir).mkdir();

        DynamicSystem dynamicSystem = Decider.newRandomTree(6, 30, rng, 0.05f);
        ////// this is just in order to write a test file //////
        try {
            FileWriter writer = new FileWriter(outputDir + "/testDecider.json");
            dynamicSystem.writeJSON(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
