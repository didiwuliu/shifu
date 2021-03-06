/**
 * Copyright [2012-2014] eBay Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ml.shifu.shifu.udf;

import org.apache.commons.io.FileUtils;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * FullScoreUDFTest class
 */
public class FullScoreUDFTest {

    private FullScoreUDF instance;
    private File tmpModels = new File("models");

    @BeforeClass
    public void setUp() throws Exception {
        File models = new File("src/test/resources/example/cancer-judgement/ModelStore/ModelSet1/models");
        FileUtils.copyDirectory(models, tmpModels);

        instance = new FullScoreUDF("LOCAL",
                "src/test/resources/example/cancer-judgement/ModelStore/ModelSet1/ModelConfig.json",
                "src/test/resources/example/cancer-judgement/ModelStore/ModelSet1/ColumnConfig.json",
                "src/test/resources/example/cancer-judgement/DataStore/DataSet1/.pig_header",
                "|");
    }

    @Test
    public void testUDFNull() throws Exception {
        Tuple tuple = TupleFactory.getInstance().newTuple(0);
        Assert.assertNull(instance.exec(tuple));
    }

    @Test
    public void testExec() throws IOException {
        Tuple input = TupleFactory.getInstance().newTuple(31);
        for (int i = 0; i < 31; i++) {
            input.set(i, 1);
        }

        Assert.assertEquals("(42,74,5,36,31,74,66,5)", instance.exec(input).toString());
    }

    @AfterClass
    public void clearUp() throws IOException {
        FileUtils.deleteDirectory(tmpModels);
    }
}
