package ml.shifu.shifu.di.spi;

import ml.shifu.shifu.container.RawValueObject;
import org.dmg.pmml.DataField;
import org.dmg.pmml.UnivariateStats;

import java.util.List;
import java.util.Map;

public interface UnivariateStatsCalculator {

    public UnivariateStats calculate(DataField field, List<? extends Object> values, Map<String, Object> params);

}
