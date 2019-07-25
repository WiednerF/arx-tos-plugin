package org.deidentifier.arx.talend.processor;

import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.BuiltInSuggestable;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.talend.sdk.component.api.configuration.ui.layout.GridLayout.FormType.MAIN;

@Data
@GridLayout(names = MAIN,value = {
    // the generated layout put one configuration entry per line,
    // customize it as much as needed
        @GridLayout.Row({"highestRisk"}),
        @GridLayout.Row({"averageRisk"}),
        @GridLayout.Row({ "recordsAtRisk" }),
        @GridLayout.Row({ "qis" })
})
@Documentation("Risk Threshold for the Configuration of the Analysis Part")
/**
 * This class encapsulates parameters related to risk management. It can be used
 * for threshold definitions as well as for storing/retrieving results.
 * @author Florian Wiedner
 */
public class ParametersRisk implements Serializable {
    /** Default value*/
    public static final double  DEFAULT_HIGHEST_RISK     = 0.2d;
    /** Default value*/
    public static final double  DEFAULT_AVERAGE_RISK     = 0.05d;
    /** Default value*/
    public static final double DEFAULT_RECORDS_AT_RISK = 0.01d;


    @Option
    @Documentation("Highest Allowed Risk in Percent")
    private double highestRisk =DEFAULT_HIGHEST_RISK;

    @Option
    @Documentation("Average Searched Risk in Percent")
    private double averageRisk =DEFAULT_AVERAGE_RISK;

    @Option
    @Documentation("Maximum amount of Records at Risk in Percent")
    private double recordsAtRisk =DEFAULT_RECORDS_AT_RISK;

    @Option
    @Documentation("Quasi-Identifier Fields")
    private List<QIValue> qis = new ArrayList<>();

    /**
     * Returns whether this object satisfies the given threshold
     * @param  thresholds ParametersRisk
     * @return boolean
     */
    public boolean satisfies(ParametersRisk thresholds) {
        return this.recordsAtRisk <= thresholds.recordsAtRisk &&
                this.averageRisk <= thresholds.averageRisk &&
                this.highestRisk <= thresholds.highestRisk;
    }

    @Data
    @Documentation("Object defining each Quasi-Identification Field")
    public static class QIValue  implements Serializable{

        @Option
        @Documentation("The Field in the Schema")
        @BuiltInSuggestable(value= BuiltInSuggestable.Name.INCOMING_SCHEMA_ENTRY_NAMES)
        private String field;

        @Option
        @Documentation("Quasi-Identifier")
        private boolean qi = true;
    }

}