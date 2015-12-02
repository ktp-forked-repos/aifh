package com.heatonresearch.aifh.ann;

import com.heatonresearch.aifh.AIFHError;
import com.heatonresearch.aifh.ann.activation.ActivationFunction;
import com.heatonresearch.aifh.ann.randomize.XaiverRandomizeNetwork;
import com.heatonresearch.aifh.learning.RegressionAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BasicNetwork implements RegressionAlgorithm {
    /**
     * The default bias activation.
     */
    public static final double DEFAULT_BIAS_ACTIVATION = 1.0;

    /**
     * The value that indicates that there is no bias activation.
     */
    public static final double NO_BIAS_ACTIVATION = 0.0;

    /**
     * The number of input neurons in this network.
     */
    private int inputCount;

    /**
     * The number of neurons in each of the layers.
     */
    private int[] layerCounts;

    /**
     * The number of context neurons in each layer. These context neurons will
     * feed the next layer.
     */
    private int[] layerContextCount;

    /**
     * The number of neurons in each layer that are actually fed by neurons in
     * the previous layer. Bias neurons, as well as context neurons, are not fed
     * from the previous layer.
     */
    private int[] layerFeedCounts;

    /**
     * An index to where each layer begins (based on the number of neurons in
     * each layer).
     */
    private int[] layerIndex;

    /**
     * The outputs from each of the neurons.
     */
    private double[] layerOutput;

    /**
     * The sum of the layer, before the activation function is applied, producing the layerOutput.
     */
    private double[] layerSums;

    /**
     * The number of output neurons in this network.
     */
    private int outputCount;

    /**
     * The index to where the weights that are stored at for a given layer.
     */
    private int[] weightIndex;

    /**
     * The weights for a neural network.
     */
    private double[] weights;

    /**
     * The activation types.
     */
    private ActivationFunction[] activationFunctions;

    /**
     * The bias activation for each layer. This is usually either 1, for a bias,
     * or zero for no bias.
     */
    private double[] biasActivation;

    private final List<Layer> layers = new ArrayList<>();

    /**
     * Default constructor.
     */
    public BasicNetwork() {
    }


    /**
     * Clear any context neurons.
     */
    public void clearContext() {
        int index = 0;

        for (int i = 0; i < this.layerIndex.length; i++) {

            final boolean hasBias = (this.layerContextCount[i] + this.layerFeedCounts[i]) != this.layerCounts[i];

            // fill in regular neurons
            Arrays.fill(this.layerOutput, index, index+this.layerFeedCounts[i], 0);
            index += this.layerFeedCounts[i];

            // fill in the bias
            if (hasBias) {
                this.layerOutput[index++] = this.biasActivation[i];
            }

            // fill in context
            Arrays.fill(this.layerOutput, index, index+this.layerContextCount[i], 0);
            index += this.layerContextCount[i];
        }
    }

    /**
     * Calculate the output for the given input.
     *
     * @param input
     *            The input.
     * @param output
     *            Output will be placed here.
     */
    public void compute(final double[] input, final double[] output) {
        final int sourceIndex = this.layerOutput.length
                - this.layerCounts[this.layerCounts.length - 1];

        System.arraycopy(input, 0, this.layerOutput, sourceIndex,
                this.inputCount);

        for (int i = 0; i<this.layerIndex.length - 1; i++) {
            this.layers.get(i).computeLayer();
        }


        System.arraycopy(this.layerOutput, 0, output, 0, this.outputCount);
    }




    /**
     * @return The activation functions.
     */
    public ActivationFunction[] getActivationFunctions() {
        return this.activationFunctions;
    }

    /**
     * @return The bias activation.
     */
    public double[] getBiasActivation() {
        return this.biasActivation;
    }

    /**
     * @return The length of the array the network would encode to.
     */
    public int getEncodeLength() {
        return this.weights.length;
    }


    /**
     * @return The number of input neurons.
     */
    public int getInputCount() {
        return this.inputCount;
    }

    /**
     * @return The layer context count.
     */
    public int[] getLayerContextCount() {
        return this.layerContextCount;
    }

    /**
     * @return The number of neurons in each layer.
     */
    public int[] getLayerCounts() {
        return this.layerCounts;
    }

    /**
     * @return The number of neurons in each layer that are fed by the previous
     *         layer.
     */
    public int[] getLayerFeedCounts() {
        return this.layerFeedCounts;
    }

    /**
     * @return Indexes into the weights for the start of each layer.
     */
    public int[] getLayerIndex() {
        return this.layerIndex;
    }

    /**
     * @return The output for each layer.
     */
    public double[] getLayerOutput() {
        return this.layerOutput;
    }

    /**
     * @return The neuron count.
     */
    public int getNeuronCount() {
        int result = 0;
        for (final int element : this.layerCounts) {
            result += element;
        }
        return result;
    }

    /**
     * @return The number of output neurons.
     */
    public int getOutputCount() {
        return this.outputCount;
    }

    /**
     * @return The index of each layer in the weight and threshold array.
     */
    public int[] getWeightIndex() {
        return this.weightIndex;
    }

    /**
     * @return The index of each layer in the weight and threshold array.
     */
    public double[] getWeights() {
        return this.weights;
    }

    /**
     * Neural networks with only one type of activation function offer certain
     * optimization options. This method determines if only a single activation
     * function is used.
     *
     * @return The number of the single activation function, or -1 if there are
     *         no activation functions or more than one type of activation
     *         function.
     */
    public Class<?> hasSameActivationFunction() {
        final List<Class<?>> map = new ArrayList<>();

        for (final ActivationFunction activation : this.activationFunctions) {
            if (!map.contains(activation.getClass())) {
                map.add(activation.getClass());
            }
        }

        if (map.size() != 1) {
            return null;
        } else {
            return map.get(0);
        }
    }

    /**
     * Set the activation functions.
     * @param af The activation functions.
     */
    public void setActivationFunctions(final ActivationFunction[] af) {
        this.activationFunctions = Arrays.copyOf(af, af.length);
    }

    /**
     * Set the bias activation.
     * @param biasActivation The bias activation.
     */
    public void setBiasActivation(final double[] biasActivation) {
        this.biasActivation = biasActivation;
    }

    /**
     * Set the input count.
     * @param inputCount The input count.
     */
    public void setInputCount(final int inputCount) {
        this.inputCount = inputCount;
    }



    /**
     * Set the output count.
     * @param outputCount The output count.
     */
    public void setOutputCount(final int outputCount) {
        this.outputCount = outputCount;
    }


    /**
     * @return the layerSums
     */
    public double[] getLayerSums() {
        return layerSums;
    }

    /**
     * Get the weight between the two layers.
     * @param fromLayer The from layer.
     * @param fromNeuron The from neuron.
     * @param toNeuron The to neuron.
     * @return The weight value.
     */
    public double getWeight(final int fromLayer,
                            final int fromNeuron,
                            final int toNeuron) {
        validateNeuron(fromLayer, fromNeuron);
        validateNeuron(fromLayer + 1, toNeuron);
        final int fromLayerNumber = this.layerContextCount.length - fromLayer - 1;
        final int toLayerNumber = fromLayerNumber - 1;

        if (toLayerNumber < 0) {
            throw new AIFHError(
                    "The specified layer is not connected to another layer: "
                            + fromLayer);
        }

        final int weightBaseIndex
                = this.weightIndex[toLayerNumber];
        final int count
                = this.layerCounts[fromLayerNumber];
        final int weightIndex = weightBaseIndex + fromNeuron
                + (toNeuron * count);

        return this.weights[weightIndex];
    }

    /**
     * Validate the the specified targetLayer and neuron are valid.
     * @param targetLayer The target layer.
     * @param neuron The target neuron.
     */
    public void validateNeuron(final int targetLayer, final int neuron) {
        if ((targetLayer < 0) || (targetLayer >= this.layerCounts.length)) {
            throw new AIFHError("Invalid layer count: " + targetLayer);
        }

        if ((neuron < 0) || (neuron >= getLayerTotalNeuronCount(targetLayer))) {
            throw new AIFHError("Invalid neuron number: " + neuron);
        }
    }

    /**
     * Get the total (including bias and context) neuron cont for a layer.
     * @param l The layer.
     * @return The count.
     */
    public int getLayerTotalNeuronCount(final int l) {
        final int layerNumber = this.layerCounts.length - l - 1;
        return this.layerCounts[layerNumber];
    }

    /**
     * Get the neuron count.
     * @param l The layer.
     * @return The neuron count.
     */
    public int getLayerNeuronCount(final int l) {
        final int layerNumber = this.layerCounts.length - l - 1;
        return this.layerFeedCounts[layerNumber];
    }

    public void addLayer(BasicLayer layer) {
        this.layers.add(layer);
    }

    public void finalizeStructure() {
        final int layerCount = layers.size();

        this.inputCount = layers.get(0).getCount();
        this.outputCount = layers.get(layerCount - 1).getCount();

        this.layerCounts = new int[layerCount];
        this.layerContextCount = new int[layerCount];
        this.weightIndex = new int[layerCount];
        this.layerIndex = new int[layerCount];

        this.activationFunctions = new ActivationFunction[layerCount];
        this.layerFeedCounts = new int[layerCount];
        this.biasActivation = new double[layerCount];

        int index = 0;
        int neuronCount = 0;
        int weightCount = 0;

        for (int i = layers.size() - 1; i >= 0; i--) {

            final Layer layer = layers.get(i);
            Layer nextLayer = null;

            if (i > 0) {
                nextLayer = layers.get(i - 1);
            }

            this.biasActivation[index] = 1;
            this.layerCounts[index] = layer.getTotalCount();
            this.layerFeedCounts[index] = layer.getCount();
            this.activationFunctions[index] = layer.getActivation();

            neuronCount += layer.getTotalCount();

            if (nextLayer != null) {
                weightCount += layer.getCount() * nextLayer.getTotalCount();
            }

            if (index == 0) {
                this.weightIndex[index] = 0;
                this.layerIndex[index] = 0;
            } else {
                this.weightIndex[index] = this.weightIndex[index - 1]
                        + (this.layerCounts[index] * this.layerFeedCounts[index - 1]);
                this.layerIndex[index] = this.layerIndex[index - 1]
                        + this.layerCounts[index - 1];
            }

            int neuronIndex = 0;
            for (int j = layers.size() - 1; j >= 0; j--) {
                neuronIndex += layers.get(j).getTotalCount();
            }

            // finalize the structure of the layer
            Layer prev = null;
            if(i<this.layers.size()-1) {
                prev = this.layers.get(i+1);
            }
            layer.finalizeStructure(this,index,prev,this.layerIndex[index], this.weightIndex[index],this.layerFeedCounts[index]);

            index++;
        }

        this.weights = new double[weightCount];
        this.layerOutput = new double[neuronCount];
        this.layerSums = new double[neuronCount];

        clearContext();
    }

    /**
     * Set the weight between the two specified neurons. The bias neuron is always
     * the last neuron on a layer.
     * @param fromLayer The from layer.
     * @param fromNeuron The from neuron.
     * @param toNeuron The to neuron.
     * @param value The to value.
     */
    public void setWeight(final int fromLayer, final int fromNeuron,
                          final int toNeuron, final double value) {
        final int fromLayerNumber = this.layers.size() - fromLayer - 1;
        final int toLayerNumber = fromLayerNumber - 1;

        if (toLayerNumber < 0) {
            throw new AIFHError(
                    "The specified layer is not connected to another layer: "
                            + fromLayer);
        }

        final int weightBaseIndex
                = getWeightIndex()[toLayerNumber];
        final int count
                = getLayerCounts()[fromLayerNumber];
        final int weightIndex = weightBaseIndex + fromNeuron
                + (toNeuron * count);

        getWeights()[weightIndex] = value;
    }

    public void reset() {
        XaiverRandomizeNetwork random = new XaiverRandomizeNetwork();
        random.randomize(this);
    }

    public List<Layer> getLayers() {
        return this.layers;
    }

    /**
     * Compute the output for the specified input.
     *
     * @param input The input.
     * @return The regression output.
     */
    @Override
    public double[] computeRegression(double[] input) {
        double[] output = new double[getOutputCount()];
        compute(input,output);
        return output;
    }

    /**
     * @return The long term memory for the algorithm.  This is usually weights or other coefficients.
     */
    @Override
    public double[] getLongTermMemory() {
        return this.weights;
    }
}