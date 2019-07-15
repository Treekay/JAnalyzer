package measurement;

import java.util.ArrayList;
import java.util.List;

import measurement.measure.SoftwareMeasure;
import measurement.metric.SoftwareStructMetric;
import measurement.metric.SoftwareStructMetricFactory;
import softwareStructure.SoftwareStructManager;
import nameTable.nameScope.NameScope;

/**
 * @author Zhou Xiaocong
 * @since 2015年7月2日
 * @version 1.0
 */
public abstract class NameScopeMeasurement {
	// The class to be measured 
	protected NameScope scope = null;		

	// The software structure manager used to calculate the measures
	protected SoftwareStructManager structManager = null;
	
	public NameScopeMeasurement(NameScope scope, SoftwareStructManager manager) {
		this.scope = scope;
		this.structManager = manager;
	}

	public NameScope getScope() {
		return scope;
	}
	
	public SoftwareStructManager getSoftwareStructManager() {
		return structManager;
	}
	
	/**
	 * Calculate the given measure, set the value, and return the measure
	 */
	public SoftwareMeasure getMeasure(SoftwareMeasure measure) {
		// Calculate the measure for the current class.
		// At first, we get an appropriate metric for calculating the measure
		SoftwareStructMetric metric = SoftwareStructMetricFactory.getMetricInstance(measure);
		if (metric == null) return measure;
		
		metric.setMeasuringObject(scope);
		metric.setSoftwareStructManager(structManager);

		metric.calculate(measure);
		return measure;
	}
	
	/**
	 * Calculate the given measure, set the value, and return the measure. This method uses String to give measure  
	 */
	public SoftwareMeasure getMeasureByIdentifier(String measureIdentifier) {
		SoftwareMeasure measure = new SoftwareMeasure(measureIdentifier);
		return getMeasure(measure);
	}
	
	/**
	 * Calculate the given measures, set the values, and return the measures
	 */
	public List<SoftwareMeasure> getMeasureList(List<SoftwareMeasure> measures) {
		for (SoftwareMeasure measure : measures) getMeasure(measure);
		return measures;
	}

	/**
	 * Calculate the given measures, set the values, and return the measures
	 */
	public List<SoftwareMeasure> getMeasureListByIdentifiers(List<String> measureIdentifiers) {
		List<SoftwareMeasure> resultList = new ArrayList<SoftwareMeasure>();
		for (String measureIdentifier : measureIdentifiers) resultList.add(getMeasureByIdentifier(measureIdentifier));
		return resultList;
	}
}
