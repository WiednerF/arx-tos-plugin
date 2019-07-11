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

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestData {
    private static ParametersRisk.QIValue age = new ParametersRisk.QIValue();
    private static ParametersRisk.QIValue sex = new ParametersRisk.QIValue();

    static void prepare(){
        age.setField("age");
        sex.setField("sex");

        if(ds1.size()==0) {
            String[][] temp = new String[][]{
                    {"M", "4711"},  // 3,       6
                    {"M", "4711"},  // 3,       6
                    {"M", "4711"},  // 3,       6
                    {"F", "4711"},  // 2,       7
                    {"M", "4711"},  // 2,       7
                    {"F", "4712"}, // 1,       2
                    {"F", "5711"},  // 1,       4
                    {"M", "5711"},   // 1,       6
                    {"M", "4711"},  // 3,       6
                    {"M", "4711"},  // 3,       6
                    {"M", "4711"},  // 3,       6
                    {"F", "4711"},  // 2,       7
                    {"M", "4711"},  // 2,       7
                    {"F", "4712"}, // 1,       2
                    {"F", "5711"},  // 1,       4
                    {"M", "5711"} ,  // 1,       6
                    {"M", "5711"},   // 1,       6
                    {"M", "4711"},  // 3,       6
                    {"M", "4711"},  // 3,       6
                    {"M", "4711"},  // 3,       6
                    {"F", "4711"},  // 2,       7
                    {"M", "4711"},  // 2,       7
                    {"F", "4712"}, // 1,       2
                    {"F", "5711"},  // 1,       4
                    {"M", "5711"},   // 1,       6
                    {"M", "5711"},   // 1,       6
                    {"M", "4711"},  // 3,       6
                    {"M", "4711"},  // 3,       6
                    {"M", "4711"},  // 3,       6
                    {"F", "4711"},  // 2,       7
                    {"M", "4711"},  // 2,       7
                    {"F", "4712"}, // 1,       2
                    {"F", "5711"},  // 1,       4
                    {"M", "5711"} ,  // 1,       6
                    {"M", "5711"},   // 1,       6
                    {"M", "4711"},  // 3,       6
                    {"M", "4711"},  // 3,       6
                    {"M", "4711"},  // 3,       6
                    {"F", "4711"},  // 2,       7
                    {"M", "4711"},  // 2,       7
                    {"F", "4712"}, // 1,       2
                    {"F", "5711"},  // 1,       4
                    {"M", "5711"}   // 1,       6
            };

            for (String[] strings : temp) {
                ds1.add(Json.createObjectBuilder()
                        .add("sex", strings[0]).add("age", strings[1]).build());
            }
        }
    }

    static List<ParametersRisk.QIValue> qis1 = Arrays.asList(sex, age);
    static List<String> input = Arrays.asList("sex","age");
    static List<JsonObject> ds1 = new ArrayList<>();
}