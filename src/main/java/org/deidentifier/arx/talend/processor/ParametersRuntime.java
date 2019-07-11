/*
 * Talend re-identification risk management RuntimeSettings
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

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Structure;
import org.talend.sdk.component.api.meta.Documentation;

import static org.talend.sdk.component.api.configuration.ui.layout.GridLayout.FormType.ADVANCED;

@Data
@GridLayout(names = ADVANCED, value={
    // the generated layout put one configuration entry per line,
    // customize it as much as needed
    @GridLayout.Row({ "Mode" }),
    @GridLayout.Row({"InputStructure"}),
    @GridLayout.Row({ "recordsPerIteration" }),
    @GridLayout.Row({ "maxQisOptimal" }),
    @GridLayout.Row({ "secondsPerIteration" }),
    @GridLayout.Row({ "snapshotSizeDataset" }),
    @GridLayout.Row({ "snapshotSizeSnapshot" }),
    @GridLayout.Row({ "cacheSize" }),
    @GridLayout.Row({ "blockSize" })
})
@Documentation("Provide the Additional Advanced Settings for the Runtime")
/**
 * This class encapsulated parameters determining the runtime behavior.
 * @author Florian Wiedner
 *
 */
public class ParametersRuntime implements Serializable {
    public enum Mode {
        ANONYMIZE,
        ASSESS
    }
    /** Default value*/
    private static final Mode    DEFAULT_MODE = Mode.ANONYMIZE;
    /** Default value*/
    private static final int     DEFAULT_BLOCK_SIZE = 0;
    /** Default value*/
    private static final  double DEFAULT_RECORDS_PER_ITERATION = 0.01d;
    /** Default value*/
    private static final  int    DEFAULT_SECONDS_PER_ITERATION = 30;
    /** Default value*/
    private static final  int    DEFAULT_MAX_QIS_OPTIMAL = 15;
    /** Default value*/
    private static final  double DEFAULT_SNAPSHOT_SIZE_DATASET = 0.2d;
    /** Default value*/
    private static final  double DEFAULT_SNAPSHOT_SIZE_SNAPSHOT = 0.8d;
    /** Default value*/
    private static final  int    DEFAULT_CACHE_SIZE = 200;


    @Option("Mode")
    @Documentation("The operation mode of the plugin")
    private Mode mode = DEFAULT_MODE;

    @Option("recordsPerIteration")
    @Documentation("Records per iteration")
    private double recordsPerIteration = DEFAULT_RECORDS_PER_ITERATION;

    @Option("maxQisOptimal")
    @Documentation("The maximum number of QIs with which an optimal anonymization is guaranteed")
    private int maxQisOptimal = DEFAULT_MAX_QIS_OPTIMAL;

    @Option("secondsPerIteration")
    @Documentation("Seconds per iteration")
    private int secondsPerIteration = DEFAULT_SECONDS_PER_ITERATION;

    @Option("snapshotSizeDataset")
    @Documentation("Maximum size of a snapshot relative to the dataset size (ARX-default is 0.2).")
    private double snapshotSizeDataset = DEFAULT_SNAPSHOT_SIZE_DATASET;

    @Option("snapshotSizeSnapshot")
    @Documentation("Maximum size of a snapshot relative to the previous snapshot (ARX-default is 0.8).")
    private double snapshotSizeSnapshot = DEFAULT_SNAPSHOT_SIZE_SNAPSHOT;

    @Option("cacheSize")
    @Documentation("Maximum number of snapshots allowed to store in the history (ARX-default is 200).")
    private int cacheSize = DEFAULT_CACHE_SIZE;

    @Option("blockSize")
    @Documentation("The block size used for row blocking. Set to zero to deactivate row-blocking")
    private int blockSize = DEFAULT_BLOCK_SIZE;

    @Option
    @Documentation("Incoming Schema of the Plugin")
    @Structure(discoverSchema = "guessTableSchema",type= Structure.Type.IN)
    private List<String> InputStructure;

    /**
     * Return, if row blocking is enabled. This is the case, if the
     * block size is greater than zero.
     * @see #getBlockSize()
     * @see #setBlockSize(int)
     * @return boolean
     */
    public boolean doRowBlocking() {
        return blockSize > 0;
    }
}