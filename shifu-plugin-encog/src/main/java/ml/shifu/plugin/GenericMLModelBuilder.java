/**
 * 
 */
package ml.shifu.plugin;


/**
 * The abstract class that converts the Machine Learing model to a PMML model
 * 
 * @param <T>
 *            The target Machine Learning model from specific Machine Learning framework such
 *            as Encog, Machout, and Spark.
 * @param <S>
 *            The source PMML model 
 */
public interface GenericMLModelBuilder<T, S> {

    /**
     * The function creates a specific Machine Learning model from PMML model.
     * 
     * @param pmmlModel
     *            The model from ML frameworks
     * @return The Machine Learning model converted from PMMl model
     */
    T createMLModelFromPMML(S pmmlModel);

}
