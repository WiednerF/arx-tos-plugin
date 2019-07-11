package org.deidentifier.arx.talend.processor;
import org.deidentifier.arx.*;
import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.metric.Metric;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.talend.sdk.component.junit.JoinInputFactory;
import org.talend.sdk.component.junit.SimpleComponentRule;
import org.talend.sdk.component.runtime.output.Processor;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ARXDeidentifierProcessorTest {

    @ClassRule
    public static final SimpleComponentRule COMPONENT_FACTORY = new SimpleComponentRule("org.deidentifier.arx.talend");

    @Before
    public void initialize() {
        TestData.prepare();
    }


   @Test
    public void anonymize(){
        //Initialization
        final ARXDeidentifierProcessorConfiguration configuration = new ARXDeidentifierProcessorConfiguration();
        configuration.setRiskSettings(new ParametersRisk());
        configuration.setRuntimeSettings(new ParametersRuntime());
        configuration.getRuntimeSettings().setMode(ParametersRuntime.Mode.ANONYMIZE);
        configuration.getRuntimeSettings().setBlockSize(500);
        configuration.getRiskSettings().setQis(TestData.qis1);
        configuration.getRuntimeSettings().setInputStructure(TestData.input);

       final Processor processor = COMPONENT_FACTORY.createProcessor(ARXDeidentifierProcessor.class, configuration);
       final JoinInputFactory joinInputFactory =  new JoinInputFactory()
               .withInput("__default__", TestData.ds1);

       final SimpleComponentRule.Outputs outputs = COMPONENT_FACTORY.collect(processor, joinInputFactory);
       assertEquals(1, outputs.size());// test of the output branches count of the component

       final List<JsonObject> defaultOutput = outputs.get(JsonObject.class, "__default__");
        //First Test
        assertEquals(TestData.ds1.size(),defaultOutput.size());//Same Number of Rows as the Input is expected
        assertNotEquals(TestData.ds1,defaultOutput);//After Anonymization the TestData should be different to the values before

    }


    @Test
    public void assess(){
        //Initialization
        final ARXDeidentifierProcessorConfiguration configuration = new ARXDeidentifierProcessorConfiguration();
        configuration.setRiskSettings(new ParametersRisk());
        configuration.setRuntimeSettings(new ParametersRuntime());
        configuration.getRuntimeSettings().setMode(ParametersRuntime.Mode.ASSESS);
        configuration.getRuntimeSettings().setBlockSize(8);
        configuration.getRiskSettings().setQis(TestData.qis1);
        configuration.getRuntimeSettings().setInputStructure(TestData.input);

        final Processor processor = COMPONENT_FACTORY.createProcessor(ARXDeidentifierProcessor.class, configuration);
        final JoinInputFactory joinInputFactory =  new JoinInputFactory()
                .withInput("__default__", TestData.ds1);

        final SimpleComponentRule.Outputs outputs = COMPONENT_FACTORY.collect(processor, joinInputFactory);
        assertEquals(1, outputs.size());// test of the output branches count of the component

        final List<JsonObject> defaultOutput = outputs.get(JsonObject.class, "Error");
        //First Test
        assertEquals(TestData.ds1.size(),defaultOutput.size());
        assertEquals(defaultOutput,TestData.ds1);//Make sure that the Data is not changed in the Process
    }
}