package measurement.metric.size;

import java.util.List;

import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.PackageDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê10ÔÂ12ÈÕ
 * @version 1.0
 */
public class TypeCounterMetric extends SoftwareSizeMetric {

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (structManager == null) return false;
		
		int nonTopType = 0;
		int topPublicType = 0;
		int topNonPublicType = 0;
		int nonTopClass = 0;
		int topPublicClass = 0;
		int topNonPublicClass = 0;
		// Note that all enumerations are not detailed type definition!!
		List<DetailedTypeDefinition> typeList = structManager.getAllDetailedTypeDefinition();
		for (DetailedTypeDefinition type : typeList) {
			if (objectScope != null) {
				if (type.getEnclosingPackage() == objectScope || objectScope == structManager.getNameTableManager().getSystemScope()) {
					if (type.isPackageMember()) {
						if (type.isPublic()) topPublicType++;
						else topNonPublicType++;
					} else nonTopType++;
					if (!type.isInterface()) {
						if (type.isPackageMember()) {
							if (type.isPublic()) topPublicClass++;
							else topNonPublicClass++;
						} else nonTopClass++;
					}
				}
			}
		}
		
		List<EnumTypeDefinition> enumList = structManager.getAllEnumTypeDefinition();
		for (EnumTypeDefinition type : enumList) {
			if (objectScope != null) {
				if (type.getEnclosingPackage() == objectScope || objectScope == structManager.getNameTableManager().getSystemScope()) {
					if (type.isPackageMember()) {
						if (type.isPublic()) topPublicType++;
						else topNonPublicType++;
					} else nonTopType++;
				}
			}
		}
		
		if (measure.match(SoftwareMeasureIdentifier.NonTopTYPE)) {
			measure.setValue(nonTopType);
		} else if (measure.match(SoftwareMeasureIdentifier.TopPubTYPE)) {
			measure.setValue(topPublicType);
		} else if (measure.match(SoftwareMeasureIdentifier.TopNonPubTYPE)) {
			measure.setValue(topNonPublicType);
		} else if (measure.match(SoftwareMeasureIdentifier.NonTopCLS)) {
			measure.setValue(nonTopClass);
		} else if (measure.match(SoftwareMeasureIdentifier.TopPubCLS)) {
			measure.setValue(topPublicClass);
		} else if (measure.match(SoftwareMeasureIdentifier.TopNonPubCLS)) {
			measure.setValue(topNonPublicClass);
		} else return false; 
		return true;
	}

	/**
	 * Set the package for calculation, if the given parameter packageDefinition is null, then
	 * calculate the metric for the whole system
	 */
	public void setPackage(PackageDefinition packageDefinition) {
		objectScope = packageDefinition;
	}
}
