/*
 * Kettle re-identification risk management step
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

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.talend.sdk.component.api.processor.OutputEmitter;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OperationDataTransformer {
    /** The String representation for NULL values */
    public final static String MAGIC_NULL_VALUE = "_TALEND_SPOON_NULL_";


    /**
     * Creates an empty dataset containing null values in all cells.
     *
     * @param cols number of columns
     * @param rows number of rows
     * @param header header
     * @return Empty String
     */
    public List<String[]> getEmptyDataset(String[] header, int cols, int rows) {
        List<String[]> ret = new ArrayList<>();

        ret.add(header);

        for (int i = 0; i < rows; i++) {
            String[] row = new String[cols];
            for (int j = 0; j < cols; j++) {
                row[j] = MAGIC_NULL_VALUE;
            }
            ret.add(row);
        }
        return ret;
    }

    /**
     * Convert data coming from the previous step of the transformation to internal format.
     *
     * @param stepData The Data from the previous Step
     * @param header the header
     * @return String[]
     */
    public String[] read(Record stepData, String header[]) {
        String[] result = new String[header.length];
        int[] index = new int[header.length];
        Schema schema = stepData.getSchema();
        schema.getEntries().forEach((element)->index[Arrays.asList(header).indexOf(element.getName())]=schema.getEntries().indexOf(element)+1);
        for (int i = 0; i < result.length; i++) {
            String string = null;
            if(index[i]!=0) {
                switch (schema.getEntries().get(index[i]-1).getType()) {
                    case INT:
                        string = Integer.toString(stepData.getInt(header[i]));
                        break;
                    case LONG:
                        string = Long.toString(stepData.getLong(header[i]) );
                        break;
                    case STRING:
                        string = stepData.getString(header[i]);
                        break;
                    case BYTES:
                        string = new String(stepData.getBytes(header[i]), StandardCharsets.UTF_8);
                        break;
                    case FLOAT:
                        string = Float.toString(stepData.getFloat(header[i]));
                        break;
                    case DOUBLE:
                        string = Double.toString(stepData.getDouble(header[i]));
                        break;
                    case BOOLEAN:
                        string = Boolean.toString(stepData.getBoolean(header[i]));
                        break;
                    case DATETIME:
                        string = stepData.getDateTime(header[i]).toString();
                        break;
                }
            }
            result[i] = string == null ? MAGIC_NULL_VALUE : string;
        }
        return result;
    }

    /**
     * Convert/prepare internal data and pass it to the next step of the transformation
     * if regularOutput==true. Else, pass all data to the error channel.
     *
     * @param payload The Data
     * @param outputMain The Main Output
     */
    public void write(List<String[]> payload, final OutputEmitter<JsonObject> outputMain){
        String[] header = payload.get(0);
        // Skip header
        for (int i=1; i<payload.size(); i++) {
            String[] rowFromBuffer = payload.get(i);
            JsonObjectBuilder builder = Json.createObjectBuilder();
            for (int j = 0; j < rowFromBuffer.length; j++) {
                String value = rowFromBuffer[j];
                if (value == null || value.equals(OperationDataTransformer.MAGIC_NULL_VALUE) || value.equals(DataType.ANY_VALUE) || value.equals(DataType.NULL_VALUE)) {
                    builder.add(header[j], JsonValue.NULL);
                }
                else {
                    builder.add(header[j],value);
                }
            }
            outputMain.emit(builder.build());
        }
    }

    /**
     * Converts a handle to a list of strings
     * @param input The Input to ARX
     * @param output the Output of ARX
     */
    public List<String[]> convert(DataHandle input, DataHandle output) {
        if (output != null) {
            List<String[]> result = new ArrayList<>();
            for (Iterator<String[]> iterator = output.iterator(); iterator.hasNext(); ) {
                result.add(iterator.next());
            }
            return result;
        } else {
            return getEmptyDataset(input.iterator().next(), input.getNumColumns(), input.getNumRows());
        }
    }

}
