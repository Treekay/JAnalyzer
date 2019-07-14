package measurement;

import java.util.ArrayList;
import java.util.List;

import measurement.measure.ClassMeasureDistribution;
import measurement.measure.SoftwareMeasure;
import softwareStructure.SoftwareStructManager;
import nameTable.NameTableManager;
import nameTable.nameDefinition.DetailedTypeDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê7ÔÂ2ÈÕ
 * @version 1.0
 */
public class SystemMeasurementManager {
	private NameTableManager tableManager = null;
	private SoftwareStructManager structManager = null;
	private List<ClassMeasurement> measureList = null;
	
	public SystemMeasurementManager(NameTableManager tableManager, SoftwareStructManager structManager) {
		this.tableManager = tableManager;
		this.structManager = structManager;
	}

	public NameTableManager getNameTableManager() {
		return tableManager;
	}

	public SoftwareStructManager getSoftwareStructManager() {
		return structManager;
	}
	
	/**
	 * Get a measure for a class (i.e. type) 
	 */
	public SoftwareMeasure getMeasure(DetailedTypeDefinition type, SoftwareMeasure measure) {
		// First, we search the measure in the buffered list (i.e. this.measureList)
		if (measureList == null) measureList = new ArrayList<ClassMeasurement>();
		for (ClassMeasurement classMeasurement : measureList) {
			if (classMeasurement.getClassType() == type) {
				return classMeasurement.getMeasure(measure);
			}
		}
		
		// Build an object of ClassStructMeasurement to calculate the measure
		ClassMeasurement classMeasurement = new ClassMeasurement(type, structManager);
		measureList.add(classMeasurement);
		return classMeasurement.getMeasure(measure);
	}

	/**
	 * Get a measure for a class (i.e. type) 
	 */
	public SoftwareMeasure getMeasure(DetailedTypeDefinition type, String measureIdentifier) {
		// First, we search the measure in the buffered list (i.e. this.measureList)
		if (measureList == null) measureList = new ArrayList<ClassMeasurement>();
		for (ClassMeasurement classMeasurement : measureList) {
			if (classMeasurement.getClassType() == type) {
				return classMeasurement.getMeasureByIdentifier(measureIdentifier);
			}
		}
		
		// Build an object of ClassStructMeasurement to calculate the measure
		ClassMeasurement classMeasurement = new ClassMeasurement(type, structManager);
		measureList.add(classMeasurement);
		return classMeasurement.getMeasureByIdentifier(measureIdentifier);
	}

	/**
	 * Get a list of measures for a class (i.e. type) 
	 */
	public List<SoftwareMeasure> getMeasureList(DetailedTypeDefinition type, List<SoftwareMeasure> measures) {
		// First, we search the measure in the buffered list (i.e. this.measureList)
		if (measureList == null) measureList = new ArrayList<ClassMeasurement>();
		for (ClassMeasurement classMeasurement : measureList) {
			if (classMeasurement.getClassType() == type) {
				return classMeasurement.getMeasureList(measures);
			}
		}
		
		// Build an object of ClassStructMeasurement to calculate the measure
		ClassMeasurement classMeasurement = new ClassMeasurement(type, structManager);
		measureList.add(classMeasurement);
		return classMeasurement.getMeasureList(measures);
	}

	/**
	 * Get a list measures for a class (i.e. type) 
	 */
	public List<SoftwareMeasure> getMeasureListByIdentifiers(DetailedTypeDefinition type, List<String> measureIdentifiers) {
		// First, we search the measure in the buffered list (i.e. this.measureList)
		if (measureList == null) measureList = new ArrayList<ClassMeasurement>();
		for (ClassMeasurement classMeasurement : measureList) {
			if (classMeasurement.getClassType() == type) {
				return classMeasurement.getMeasureListByIdentifiers(measureIdentifiers);
			}
		}
		
		// Build an object of ClassStructMeasurement to calculate the measure
		ClassMeasurement classMeasurement = new ClassMeasurement(type, structManager);
		measureList.add(classMeasurement);
		return classMeasurement.getMeasureListByIdentifiers(measureIdentifiers);
	}

	/**
	 * Get a list of measures for a list of class (i.e. type) 
	 */
	public List<ClassMeasurement> getClassMeasurementList(List<DetailedTypeDefinition> types, List<SoftwareMeasure> measures) {
		List<ClassMeasurement> resultList = new ArrayList<ClassMeasurement>();
		
		// First, we search the measure in the buffered list (i.e. this.measureList)
		if (measureList == null) measureList = new ArrayList<ClassMeasurement>();
		
		for (DetailedTypeDefinition type : types) {
			for (ClassMeasurement classMeasurement : measureList) {
				if (classMeasurement.getClassType() == type) {
					// To calculate the measures in the given list (i.e. the parameter measures).
					classMeasurement.getMeasureList(measures);
					resultList.add(classMeasurement);
				}
			}
			
			// Build an object of ClassStructMeasurement to calculate the measure
			ClassMeasurement classMeasurement = new ClassMeasurement(type, structManager);
			measureList.add(classMeasurement);
			// To calculate the measures in the given list (i.e. the parameter measures).
			classMeasurement.getMeasureList(measures);
			resultList.add(classMeasurement);
		}
		return resultList;
	}

	/**
	 * Get a list of measures for a list of class (i.e. type) 
	 */
	public List<ClassMeasurement> getClassMeasurementListByIdentifiers(List<DetailedTypeDefinition> types, List<String> measureIdentifiers) {
		List<ClassMeasurement> resultList = new ArrayList<ClassMeasurement>();
		
		// First, we search the measure in the buffered list (i.e. this.measureList)
		if (measureList == null) measureList = new ArrayList<ClassMeasurement>();
		
		for (DetailedTypeDefinition type : types) {
			for (ClassMeasurement classMeasurement : measureList) {
				if (classMeasurement.getClassType() == type) {
					// To calculate the measures in the given list (i.e. the parameter measures).
					classMeasurement.getMeasureListByIdentifiers(measureIdentifiers);
					resultList.add(classMeasurement);
				}
			}
			
			// Build an object of ClassStructMeasurement to calculate the measure
			ClassMeasurement classMeasurement = new ClassMeasurement(type, structManager);
			measureList.add(classMeasurement);
			// To calculate the measures in the given list (i.e. the parameter measures).
			classMeasurement.getMeasureListByIdentifiers(measureIdentifiers);
			resultList.add(classMeasurement);
		}
		return resultList;
	}
	
	/**
	 * Get a distribution of a measure for a list of class.
	 */
	public ClassMeasureDistribution getMeasureDistributionOfClassList(List<DetailedTypeDefinition> types, SoftwareMeasure measure) {
		ClassMeasureDistribution result = new ClassMeasureDistribution(measure);
		result.setClassTypeList(types);
		
		for (DetailedTypeDefinition type : types) {
			// Set the last value is not usable!
			measure.setUnusable();
			measure = getMeasure(type, measure);
			if (measure.isUsable()) {
				result.setValue(type, measure.getValue());
			}
		}
		result.setUsable();
		return result;
	}
	
	/**
	 * Get a distribution of a measure (given by its identifier) for a list of class.
	 */
	public ClassMeasureDistribution getMeasureDistributionOfClassList(List<DetailedTypeDefinition> types, String identifier) {
		SoftwareMeasure measure = new SoftwareMeasure(identifier);
		
		ClassMeasureDistribution result = new ClassMeasureDistribution(measure);
		result.setClassTypeList(types);
		
		for (DetailedTypeDefinition type : types) {
			// Set the last value is not usable!
			measure.setUnusable();
			measure = getMeasure(type, measure);
			if (measure.isUsable()) {
				result.setValue(type, measure.getValue());
			}
		}
		result.setUsable();
		return result;
	}
}
