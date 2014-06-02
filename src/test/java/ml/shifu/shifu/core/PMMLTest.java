package ml.shifu.shifu.core;

import ml.shifu.shifu.container.CategoricalValueObject;
import ml.shifu.shifu.container.NumericalValueObject;
import ml.shifu.shifu.container.RawValueObject;
import ml.shifu.shifu.di.builtin.BinomialUnivariateStatsCalculator;
import ml.shifu.shifu.di.builtin.DefaultUnivariateStatsCalculator;
import ml.shifu.shifu.di.builtin.TripletDataDictionaryInitializer;
import ml.shifu.shifu.di.spi.SingleThreadFileLoader;
import ml.shifu.shifu.di.spi.UnivariateStatsCalculator;
import ml.shifu.shifu.util.CSVWithHeaderLocalSingleThreadFileLoader;
import ml.shifu.shifu.util.LocalDataTransposer;
import org.dmg.pmml.*;
import org.jpmml.model.JAXBUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

public class PMMLTest {


    @Test
    public void testOutputXML() {
        List<NumericalValueObject> nvoList = new ArrayList<NumericalValueObject>();
        for (int i = 0; i < 100; i++) {
            NumericalValueObject nvo = new NumericalValueObject();
            nvo.setValue(i * 1.0 % 39);
            nvo.setIsPositive(i % 2 == 0 ? true : false);
            nvoList.add(nvo);
        }


        List<CategoricalValueObject> cvoList = new ArrayList<CategoricalValueObject>();

        for (int i = 0; i < 100; i++) {
            CategoricalValueObject vo = new CategoricalValueObject();
            vo.setValue(i % 3 == 1 ? "Cat" : "Dog");
            vo.setIsPositive(i % 2 == 1 ? true : false);
            cvoList.add(vo);
        }




        OutputStream os = null;


        PMML pmml = new PMML();
        Model model = new NeuralNetwork();
        ModelStats modelStats = new ModelStats();

        UnivariateStats univariateStats = new UnivariateStats();
        //UnivariateStatsContCalculator.calculate(univariateStats, nvoList, 10);
        UnivariateStatsDiscrCalculator.calculate(univariateStats, cvoList, null);

        modelStats.withUnivariateStats(univariateStats);
        model.setModelStats(modelStats);
        pmml.withModels(model);

        try {
            os = new FileOutputStream("test.xml");
            StreamResult result = new StreamResult(os);
            JAXBUtil.marshalPMML(pmml, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadData() {

        TripletDataDictionaryInitializer initializer = new TripletDataDictionaryInitializer();

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("filePath", "src/test/resources/conf/IrisFields.txt");

        DataDictionary dict = initializer.init(params);

        SingleThreadFileLoader loader = new CSVWithHeaderLocalSingleThreadFileLoader();

        List<List<String>> rows = loader.load("src/test/resources/unittest/DataSet/iris/iris.csv");

        List<List<String>> columns = LocalDataTransposer.transpose(rows);


        Assert.assertEquals((int)dict.getNumberOfFields(), columns.size());


        Map<String, Object> statsParams = new HashMap<String, Object>();

        statsParams.put("tags", columns.get(4));
        statsParams.put("numBins", 10);
        statsParams.put("posTags", Arrays.asList("Iris-setosa", "Iris-versicolor"));
        statsParams.put("negTags", Arrays.asList("Iris-virginica"));

        PMML pmml = new PMML();
        Model model = new NeuralNetwork();
        ModelStats modelStats = new ModelStats();

        int size = dict.getNumberOfFields();
        for (int i = 0; i < size; i++) {

            DataField field = dict.getDataFields().get(i);
            List<String> column = columns.get(i);


            UnivariateStatsCalculator univariateStatsCalculator = new BinomialUnivariateStatsCalculator();

            UnivariateStats univariateStats = univariateStatsCalculator.calculate(field, column, statsParams);
            modelStats.withUnivariateStats(univariateStats);

        }


        OutputStream os = null;

        model.setModelStats(modelStats);
        pmml.withModels(model);

        try {
            os = new FileOutputStream("test.xml");
            StreamResult result = new StreamResult(os);
            JAXBUtil.marshalPMML(pmml, result);

        } catch (Exception e) {
            e.printStackTrace();
        }




    }
}
