/*
 * Talend re-identification risk management step
 * Copyright (C) 2018 TUM/MRI
 * Copyright (C) 2019 Florian Wiedner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.deidentifier.arx.talend.processor;

import java.util.List;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelSampleWildcard;

/**
 * This class encapsulates methods to calculate different types of re-identification risks.
 * @author Fabian Prasser
 * @author Helmut Spengler
 * @author Florian Wiedner
 */
public class OperationRiskAssessment {
    /** Parameters relating to risk management. */
    private final ParametersRisk thresholds;

    /**
     * Constructor.
     * @param thresholds ParametersRisk
     */
    public OperationRiskAssessment(ParametersRisk thresholds) {
        this.thresholds = thresholds;
    }

    /**
     * Calculate the different risk values for the dataset.
     *
     * @param data List<String[]> data
     * @return
     */
    public ParametersRisk calculate(List<String[]> data) {

        // Create ARX data object from previously filled rowList object
        Data arxData = Data.create(data);

        // Configure QI settings
        for (ParametersRisk.QIValue qi : this.thresholds.getQis()) {
            if(qi.isQi()) {
                arxData.getDefinition().setAttributeType(qi.getField(), AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
            }
        }

        // Prepare input, estimator, and output variables
        DataHandle handle = arxData.getHandle();
        RiskEstimateBuilder builder = handle.getRiskEstimator();
        ParametersRisk result = new ParametersRisk();

        // Build risk model
        RiskModelSampleWildcard riskModel = builder.getSampleBasedRiskSummaryWildcard((thresholds.getHighestRisk()), OperationDataTransformer.MAGIC_NULL_VALUE);


        result.setHighestRisk(thresholds.getHighestRisk()); // riskModel.getHighestRisk() is *not* what we want here
        result.setRecordsAtRisk(riskModel.getRecordsAtRisk());
        result.setAverageRisk(riskModel.getAverageRisk());

        return result;
    }
}
