package org.deidentifier.arx.talend.processor;

import java.io.Serializable;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.json.*;

import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.AfterGroup;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Input;
import org.talend.sdk.component.api.processor.Output;
import org.talend.sdk.component.api.processor.OutputEmitter;
import org.talend.sdk.component.api.processor.Processor;
import org.talend.sdk.component.api.record.Record;

@Version(1) // default version is 1, if some configuration changes happen between 2 versions you can add a migrationHandler
@Icon(value = Icon.IconType.CUSTOM, custom="logo")
@Processor(name = "ARXDeidentifier")
@Documentation("Automatically Anonymize or Assess Data")
public class ARXDeidentifierProcessor implements Serializable {

    private final ARXDeidentifierProcessorConfiguration configuration;
    private List<String[]> buffer;
    private boolean first;
    private String[] header;
    private ParametersStatistics statistics;

    public ARXDeidentifierProcessor(@Option("configuration") final ARXDeidentifierProcessorConfiguration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    public void init() {
        this.buffer=new ArrayList<>();
        this.first=true;
        this.statistics = new ParametersStatistics();
    }

    @ElementListener
    public void bufferizer(
            @Input final Record defaultInput, @Output final OutputEmitter<JsonObject> outputMain,@Output("Error") final OutputEmitter<JsonObject> error) {
        if(this.configuration.getRuntimeSettings().doRowBlocking()&&this.configuration.getRuntimeSettings().getBlockSize()<this.buffer.size()){
            this.rowProcessing(outputMain,error);
            this.buffer=new ArrayList<>();
            this.buffer.add(this.header);
        }

        if(this.first){
            //Reset first (Only executed once)
            this.first=false;
            this.header = new String[this.configuration.getRuntimeSettings().getInputStructure().size()];
            this.configuration.getRuntimeSettings().getInputStructure().forEach((element)->this.header[this.configuration.getRuntimeSettings().getInputStructure().indexOf(element)]=element);//Needs only be done once as all entries are the Same.
            for (ParametersRisk.QIValue qi : this.configuration.getRiskSettings().getQis()) {
                if (!Arrays.asList(this.header).contains(qi.getField())) {
                    System.err.println("Quasi-Identifier not in List");
                }
            }
            this.buffer.add(this.header);
            this.buffer.add(new OperationDataTransformer().read(defaultInput,this.header));
        }else{
            this.buffer.add(new OperationDataTransformer().read(defaultInput,this.header));
        }

    }


    private void rowProcessing(final OutputEmitter<JsonObject> outputMain, final OutputEmitter<JsonObject> error){
        switch(this.configuration.getRuntimeSettings().getMode()){
            case ANONYMIZE:
                OperationCellSuppression op = new OperationCellSuppression(configuration.getRiskSettings(), configuration.getRuntimeSettings());
                List<String[]> output;
                try {
                    output=op.perform(this.buffer);
                }catch(Exception $e){
                    output = new OperationDataTransformer().getEmptyDataset(this.header,this.header.length,this.buffer.size());
                }
                this.statistics.trackSuppressedCells(this.buffer, output);
                new OperationDataTransformer().write(output,outputMain);
                break;
            case ASSESS:
                OperationRiskAssessment assessment = new OperationRiskAssessment(this.configuration.getRiskSettings());
                ParametersRisk risk = assessment.calculate(this.buffer);
                this.statistics.trackRisks(risk, this.buffer.size()-1);
                if (!risk.satisfies(this.configuration.getRiskSettings())) { // Check if risks are fulfilled
                    new OperationDataTransformer().write(this.buffer,error);
                }else{
                    new OperationDataTransformer().write(this.buffer,outputMain);
                }
                break;
        }
    }

    @AfterGroup
    public void endBatch(@Output final OutputEmitter<JsonObject> outputMain,@Output("Error") final OutputEmitter<JsonObject> error) {
        // symmetric method of the beforeGroup() executed after the chunk processing
        // Note: if you don't need it you can delete it
        if(buffer!=null&&this.configuration.getRuntimeSettings().doRowBlocking()){
            this.rowProcessing(outputMain,error);
            this.buffer=new ArrayList<>();
            this.buffer.add(this.header);
        }
        if(this.configuration.getRuntimeSettings().getMode()== ParametersRuntime.Mode.ASSESS){
            System.out.println("done");
        }else{
            System.out.println("Fraction of suppressed cells: " + this.statistics.getFractionOfSuppressedCells());
        }
    }

    @PreDestroy
    public void release() {
        // this is the symmetric method of the init() one,
        // release potential connections you created or data you cached
        // Note: if you don't need it you can delete it
        statistics = null;
        header = null;
        buffer=null;
    }
}