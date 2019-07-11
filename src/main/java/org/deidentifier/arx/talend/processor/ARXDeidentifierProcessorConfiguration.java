package org.deidentifier.arx.talend.processor;

import java.io.Serializable;

import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayouts;
import org.talend.sdk.component.api.meta.Documentation;

@Data
@GridLayouts({
        @GridLayout(names = GridLayout.FormType.MAIN,value={
                    @GridLayout.Row({ "riskSettings"})
        }),
        @GridLayout(names = GridLayout.FormType.ADVANCED,value={
                @GridLayout.Row({ "runtimeSettings" })
        })
})

@Documentation("Required Configuration to optimal use the algorithm to help people")
public class ARXDeidentifierProcessorConfiguration implements Serializable {

    @Option("riskSettings")
    @Documentation("Setting the Configuration Related to the Risk after the Processing")
    private ParametersRisk riskSettings;

    @Option("runtimeSettings")
    @Documentation("Setting the Runtime Information Required to Assess the Development")
    private ParametersRuntime runtimeSettings;

}