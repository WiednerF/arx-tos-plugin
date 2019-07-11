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

import java.io.IOException;
import java.util.List;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.metric.Metric;

/**
 * This class performs all operations related to cell suppression, using ARX.
 * @author Helmut Spengler
 * @author Fabian Prasser
 * @author Florian Wiedner
 */
public class OperationCellSuppression {
    /** Parameters relating to risk management. */
    private final ParametersRisk risk;

    /** Parameters determining runtime behavior. */
    private final ParametersRuntime runtime;

    /**
     * Constructor.
     *
     * @param risk ParametersRisk
     * @param runtime ParametersRuntime
     */
    public OperationCellSuppression(ParametersRisk risk, ParametersRuntime runtime) {
        this.risk = risk;
        this.runtime = runtime;
    }


    /**
     * Perform cell suppression
     *
     * @param data List<String[]> All the Data from the Input
     * @return List<String[]>
     * @throws Exception Old Exception Style
     */
    public List<String[]> perform(List<String[]> data) throws Exception {
        int numDataRows = data.size() - 1;
        int k = getSizeThreshold(risk.getHighestRisk());
        if (k > numDataRows) {
            return new OperationDataTransformer().getEmptyDataset(data.get(0), data.get(0).length, numDataRows);
        }
        // Load data
        Data arxData = Data.create(data);
        // Configure QI settings
        for (String attr : data.get(0)) {
            arxData.getDefinition().setAttributeType(attr, AttributeType.INSENSITIVE_ATTRIBUTE);
        }
        for (ParametersRisk.QIValue qi : this.risk.getQis()) {
            if(qi.isQi()) {
                Hierarchy hierarchy = getHierarchy(arxData, qi.getField());
                arxData.getDefinition().setAttributeType(qi.getField(), hierarchy);
            }
        }
        // Configure algorithm
        ARXConfiguration config = ARXConfiguration.create();
        double o_min = runtime.getRecordsPerIteration();
        double maxOutliers = 1.0d - o_min;

        config.setSuppressionLimit(maxOutliers);
        config.setQualityModel(Metric.createLossMetric(0d));

        if (risk.getRecordsAtRisk() == 0d) {
            if (k != 1) {
                config.addPrivacyModel(new KAnonymity(k));
            }
            if (risk.getAverageRisk() != 1d) {
                config.addPrivacyModel(new AverageReidentificationRisk(risk.getAverageRisk()));
            }
        } else {
            config.addPrivacyModel(new AverageReidentificationRisk((risk.getAverageRisk()), (risk.getHighestRisk()), (risk.getRecordsAtRisk())));
        }
        config.setHeuristicSearchTimeLimit(runtime.getSecondsPerIteration() * 1000);
        config.setHeuristicSearchEnabled(risk.getQis().size() > runtime.getMaxQisOptimal());
        arxData.getHandle();
        // Perform anonymization
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        anonymizer.setMaximumSnapshotSizeDataset((runtime.getSnapshotSizeDataset()));
        anonymizer.setMaximumSnapshotSizeSnapshot((runtime.getSnapshotSizeSnapshot()));
        anonymizer.setHistorySize(runtime.getCacheSize());
        ARXResult result;
        try {
            result = anonymizer.anonymize(arxData, config);
        } catch (IOException e) {
            throw new Exception(e);
        }
        DataHandle output = result.getOutput();

        if (output != null && result.isOptimizable(output)) {
            try {
                result.optimizeIterativeFast(output, o_min);
            } catch (RollbackRequiredException e) {
                throw new Exception(e);
            }
        }
        // Return
        List<String[]> retval = new OperationDataTransformer().convert(arxData.getHandle(), output);

        arxData.getHandle().release();
        return retval;
    }


    /**
     * Returns a minimal class size for the given risk threshold.
     * @author Helmut Spengler
     * @author Fabian Prasser
     * @param riskThreshold Risk Threshold
     * @return int
     */
    private int getSizeThreshold(double riskThreshold) {
        double size = 1d / riskThreshold;
        double floor = Math.floor(size);
        if ((1d / floor) - (1d / size) >= 0.01d * riskThreshold) {
            floor += 1d;
        }
        return (int) floor;
    }

    /**
     * Returns the generalization hierarchy for the dataset and attribute
     *
     * @param  data Data
     * @param  attribute String
     * @return Hierarchy
     */
    private Hierarchy getHierarchy(Data data, String attribute){
        DefaultHierarchy hierarchy = Hierarchy.create();
        int col = data.getHandle().getColumnIndexOf(attribute);
        String[] values = data.getHandle().getDistinctValues(col);
        for (String value : values) {
            hierarchy.add(value, OperationDataTransformer.MAGIC_NULL_VALUE);
        }
        return hierarchy;
    }
}
