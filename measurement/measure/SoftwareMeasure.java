package measurement.measure;

/**
 * The object of this class is used to store the measure value
 * @author Zhou Xiaocong
 * @since 2015Äê7ÔÂ2ÈÕ
 * @version 1.0
 */
public class SoftwareMeasure {
	protected String identifier = null;
	protected double value = 0;
	protected boolean usable = false;

	public SoftwareMeasure(String identifier) {
		this.identifier = identifier;
	}

	public SoftwareMeasure(SoftwareMeasure measure) {
		this.identifier = measure.identifier;
		this.value = measure.value;
		this.usable = measure.usable;
	}
	
	public boolean match(String identifier) {
		if (identifier == null || this.identifier == null) return false;
		return identifier.equals(this.identifier);
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public double getValue() {
		return value;
	}


	public boolean isUsable() {
		return usable;
	}


	/**
	 * Set the value, and set the measure to be usable
	 */
	public void setValue(double value) {
		this.value = value;
		this.usable = true;
	}

	/**
	 * Set the measure to be usable
	 */
	public void setUsable() {
		this.usable = true;
	}
	
	/**
	 * Set the measure to be unusable (i.e. to be not usable)
	 */
	public void setUnusable() {
		this.usable = false;
	}
	
	/**
	 * Show the identifier and value for debugging
	 */
	public String toString() {
		return identifier + "[" + value + "]";
	}
	
	/**
	 * Return a string to represent the value for printing
	 * If the measure is usable, the return the string of value, else return "NA"
	 */
	public String valueString() {
		if (isUsable()) return value+"";
		else return "NA";
	}
}
