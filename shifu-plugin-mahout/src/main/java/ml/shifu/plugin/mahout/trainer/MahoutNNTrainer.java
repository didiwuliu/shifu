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

package ml.shifu.plugin.mahout.trainer;

import java.io.File;
import java.io.IOException;

import ml.shifu.core.container.HiddenLayer;
import ml.shifu.core.container.NNParams;
import ml.shifu.core.container.PMMLDataSet;
import ml.shifu.core.util.PMMLUtils;
import ml.shifu.core.util.Params;

import org.apache.mahout.classifier.mlp.MultilayerPerceptron;
import org.apache.mahout.classifier.mlp.NeuralNetwork;
import org.apache.mahout.math.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MahoutNNTrainer extends MahoutAbstractTrainer {

	private static Logger log = LoggerFactory.getLogger(MahoutNNTrainer.class);
	private NeuralNetwork network;

	public Object train(PMMLDataSet dataSet, Params rawParams) throws Exception {
		NNParams params = parseParams(rawParams);
		String trainerID = rawParams.get("trainerID").toString();
		String pathOutput = rawParams.get("pathOutput").toString();
		File outputFolder = new File(pathOutput);
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}
		Integer numActiveFields = PMMLUtils.getNumActiveMiningFields(dataSet
				.getMiningSchema());
		Integer numTargetFields = PMMLUtils.getNumTargetMiningFields(dataSet
				.getMiningSchema());
		// prepare data set
		convertDataSet(dataSet, numActiveFields, numTargetFields);
		splitDataSet(params.getSplitRatio());
		// create neural network
		NeuralNetwork network = createNetwork(params, numActiveFields,
				numTargetFields);
		// train the data
		for (MahoutDataPair input : fullDataSet) {
			if (!input.isEvalData)
				network.trainOnline(input.getMahoutInputVector());
		}
		// save neural network
		String path = pathOutput;
		saveMLModel(path);
		// evaluate and calculate errors
		String extra = " <-- NN saved: " + path;
		log.info("  Trainer-" + trainerID + "\n Train Error: "
				+ df.format(getTestSetError()) + "\n" + extra);
		log.info("Trainer #" + trainerID + " is Finished!");
		return network;
	}

	private NNParams parseParams(Params rawParams) throws Exception {
		ObjectMapper jsonMapper = new ObjectMapper();
		String jsonString = jsonMapper.writeValueAsString(rawParams);
		return jsonMapper.readValue(jsonString, NNParams.class);
	}

	private NeuralNetwork createNetwork(NNParams params, int inputSize,
			int outputSize) {
		network = new MultilayerPerceptron();
		network.addLayer(inputSize, false, "Identity");

		for (HiddenLayer hiddenLayer : params.getHiddenLayers()) {

			String activationFunction = hiddenLayer.getActivationFunction();
			int numHiddenNodes = hiddenLayer.getNumHiddenNodes();
			if (activationFunction.equalsIgnoreCase("Identity")) {
				network.addLayer(numHiddenNodes, false, "Identity");
			} else if (activationFunction.equalsIgnoreCase("Sigmoid")) {
				network.addLayer(numHiddenNodes, false, "Sigmoid");
			} else {
				throw new RuntimeException("Unsupported ActivationFunction: "
						+ activationFunction);
			}
		}
		network.addLayer(outputSize, true, "Sigmoid");

		return network;
	}

	private void saveMLModel(String path) throws IOException {

		// EncogDirectoryPersistence.saveObject(new File(path), network);
	}

	private Double calculateMSE() {
		double mseError = 0;
		long numRecords = fullDataSet.size();
		for (MahoutDataPair pair : fullDataSet) {
			if (!pair.isEvalData)
				continue;
			Vector predict = network.getOutput(pair.getMahoutEvalVector());
			int len = predict.getNumNondefaultElements();
			double[] idealData = pair.getIdealData();
			if (idealData.length != len)
				throw new RuntimeException(
						"input evaluation data fields does not match with the evaluation data field of the model");

			double[] variants = new double[idealData.length];
			for (int i = 0; i < len; i++) {
				variants[i] = idealData[i] - predict.get(i);
			}
			mseError += Math.pow(variants[0], 2.0);
		}
		return mseError / numRecords;
	}

	public double getTestSetError() {
		return calculateMSE();
	}
}
