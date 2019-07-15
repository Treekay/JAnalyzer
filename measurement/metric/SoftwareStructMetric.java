package measurement.metric;

import measurement.measure.SoftwareMeasure;
import nameTable.nameScope.NameScope;
import softwareStructure.SoftwareStructManager;

/**
 * @author Zhou Xiaocong
 * @since 2015年7月2日
 * @version 1.0
 */
public interface SoftwareStructMetric {
	
	/**
	 * Set the software structure manager for calculate a measure.
	 * Note use structManager.getNameTableManager() can get the name table manager of the measuring system.
	 * Also use structManager.getNameTableManager().getSourceCodeParser() can get the source code parser of the measuring system
	 */
	public void setSoftwareStructManager(SoftwareStructManager structManager);

	/**
	 * Set the measuring object for calculate a measure
	 */
	public void setMeasuringObject(NameScope objectScope);

	/**
	 * Calculate a measure, if getting a usable measure value return true, else return false. 
	 * The result measure value are stored in the SoftareMeausre object measure.
	 * If this method return true, then measure.isUsable() must be true too!!
	 */
	public boolean calculate(SoftwareMeasure measure);

}
