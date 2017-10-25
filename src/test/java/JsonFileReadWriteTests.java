import junit.framework.TestCase;
import net.happybrackets.patternspace.dynamic_system.core.DynamicSystem;
import net.happybrackets.patternspace.dynamic_system.core.DynamicSystemUtils;
import net.happybrackets.patternspace.dynamic_system.ctrnn.Ctrnn;
import net.happybrackets.patternspace.dynamic_system.decider.Decider;
import org.junit.Test;

import java.io.*;
import java.util.Random;

public class JsonFileReadWriteTests extends TestCase {

    String dataDir = "build/tmp/testOutput";

    public void setUp() throws Exception {
        if(!new File(dataDir).exists()) new File(dataDir).mkdir();
    }

//    @Test
//    public void testWritingDeciderToJson()  throws Exception {
//        Random rng = new Random();
//        DynamicSystem dynamicSystem = Decider.newRandomTree(6, 30, rng, 0.05f);
//        FileWriter writer = new FileWriter(dataDir + "/testDecider.json");
//        DynamicSystemUtils.writeToJSON(dynamicSystem, writer);
//        writer.close();
//    }

//    @Test
//    public void testReadingDeciderFromJson() throws Exception {
//        FileReader reader = new FileReader(dataDir + "/testDecider.json");
//        DynamicSystem dynamicSystem = DynamicSystemUtils.readFromJSON(reader);
//        reader.close();
//        dynamicSystem.update(new Number[]{0.0});
//        dynamicSystem.getOutputs();
//        assert dynamicSystem != null;
//    }

    @Test
    public void testWritingCtrnnToJson()  throws Exception {
        Random rng = new Random();
        Ctrnn.Params params = Ctrnn.Params.getDefault();
        params.numInputNodes = 1;
        params.numOutputNodes = 1;
        params.numHiddenNodes = 2;
        DynamicSystem dynamicSystem = new Ctrnn(params);
        FileWriter writer = new FileWriter(dataDir + "/testCtrnn.json");
        DynamicSystemUtils.writeToJSON(dynamicSystem, writer);
        writer.close();
    }

    @Test
    public void testReadingCtrnnFromJson() throws Exception {
        FileReader reader = new FileReader(dataDir + "/testCtrnn.json");
        DynamicSystem dynamicSystem = DynamicSystemUtils.readFromJSON(reader);
        reader.close();
        dynamicSystem.update(new Number[]{0.0});
        dynamicSystem.getOutputs();
        assert dynamicSystem != null;
    }

    @Test
    public void testWritingDeciderToBinary() throws Exception {
        Random rng = new Random();
        DynamicSystem dynamicSystem = Decider.newRandomTree(6, 30, rng, 0.05f);
        FileOutputStream fos = new FileOutputStream(new File(dataDir + "/testDeciderBinary"));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(dynamicSystem);
        oos.close();
        fos.close();
    }

    @Test
    public void testReadingDeciderFromBinary() throws Exception {
        Decider decider = Decider.read(dataDir + "/testDeciderBinary");
        decider.update(new Number[]{0.0});
        decider.getOutputs();
    }


}
