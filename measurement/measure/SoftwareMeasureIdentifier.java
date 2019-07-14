package measurement.measure;

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the frequently used software measure identifiers
 * @author Zhou Xiaocong
 * @since 2015年7月2日
 * @version 1.0
 */
public class SoftwareMeasureIdentifier {
	// Size metrics. The following metrics will consider all items in the given name scope!
	public static final String FILE = "FILE";		
	public static final String BYTE = "BYTE";		
	public static final String ELOC = "ELOC";
	public static final String CLOC = "CLOC";
	public static final String BLOC = "BLOC";
	public static final String NLOC = "NLOC";
	public static final String LOC = "LOC";
	public static final String LOPT = "LOPT";
	public static final String WORD = "WORD";
	public static final String CHAR = "CHAR";

	public static final String PKG = "PKG";			
	public static final String CLS = "CLS";			
	public static final String INTF = "INTF";
	public static final String ENUM = "ENUM";
	public static final String FLD = "FLD";
	public static final String MTHD = "MTHD";
	public static final String PARS = "PARS";
	public static final String LOCV = "LOCV";
	public static final String STMN = "STMN";
	public static final String LOOPSTMN = "LOOPSTMN";
	public static final String BRANCHSTMN = "BRANCHSTMN";

	public static final String CFGNODE = "CFGNODE";
	public static final String CFGPREDICATE = "CFGPREDICATE";
	
	// The number of non-package member types in the given package or in the whole system 
	public static final String NonTopTYPE = "NonTopType";
	// The number of package member and public types in the given package or in the whole system 
	public static final String TopPubTYPE = "TopPubType";
	// The number of package member and non-public types in the given package or in the whole system 
	public static final String TopNonPubTYPE = "TopNonPubType";
	// Note that the metric CLS+INTF+ENUM gives all types in the given name scope (include package or the whole system), 
	// and then, for a package or the whole system, CLS+INTF+ENUM = NonTopType + TopPubType + TopNonPubType
	
	// The number of non-package member classes in the given package or in the whole system 
	public static final String NonTopCLS = "NonTopCLS";
	// The number of package member and public classes in the given package or in the whole system 
	public static final String TopPubCLS = "TopPubCLS";
	// The number of package member and non-public classes in the given package or in the whole system 
	public static final String TopNonPubCLS = "TopNonPubCLS";
	// Note that the metric CLS gives all classes in the given name scope (include package or the whole system), 
	// and then, for a package or the whole system, CLS = NonTopCLS + TopPubCLS + TopNonPubCLS

	// The number of all fields (include inherited fields) in the given class
	public static final String ALLFLD = "ALLFLD";
	// The number of all inherited fields in the given class
	public static final String IHFLD = "IHFLD";
	// Note that the metric FLD can give all implemented fields in the given class, and then for a class, ALLFLD = IHFLD + FLD
	
	// The number of all methods (include inherited methods) in the given class
	public static final String ALLMTHD = "ALLMTHD";
	// The number of inherited methods in the given class
	public static final String IHMTHD = "IHMTHD";
	// The number of new methods in the given class, and then ALLMTHD = IHMTHD + NEWMTHD + OVMTHD
	public static final String NEWMTHD = "NEWMTHD";
	// The number of override methods in the given class
	public static final String OVMTHD = "OVMTHD";
	// Note that the metric MTHD can give all method declared in the given class 		

	// The number of implemented methods in the given class
	public static final String IMPMTHD = "IMPMTHD";
	
	// Cohesion metrics
	public static final String LCOM1 = "LCOM1";
	public static final String LCOM1p = "LCOM1p";
	public static final String LCOM2 = "LCOM2";
	public static final String LCOM2p = "LCOM2p";
	public static final String LCOM3 = "LCOM3";
	public static final String LCOM4 = "LCOM4";
	public static final String Co = "Co";
	public static final String CoPrim = "CoPrim";
	public static final String LCOM5 = "LCOM5";
	public static final String Coh = "Coh";
	public static final String TCC = "TCC";
	public static final String TCCp = "TCCp";
	public static final String LCC = "LCC";
	public static final String LCCp = "LCCp";
	public static final String DCD = "DCD";
	public static final String DCDp = "DCDp";
	public static final String DCI = "DCI";
	public static final String DCIp = "DCIp";
	public static final String OCC = "OCC";
	public static final String PCC = "PCC";
	public static final String CC = "CC";
	public static final String CCp = "CCp";
	public static final String SCOM = "SCOM";
	public static final String SCOMp = "SCOMp";
	public static final String LSCC = "LSCC";
	public static final String CBMC = "CBMC";
	public static final String ICBMC = "ICBMC";
	public static final String PCCC = "PCCC";
	public static final String ICH = "ICH";
	public static final String CAMC = "CAMC";
	public static final String NHD = "NHD";
	public static final String SNHD = "SNHD";
	public static final String SCC = "SCC";
	
	// Coupling metrics
	public static final String CBO = "CBO";
	public static final String CBOi = "CBOi";
	public static final String CBOe = "CBOe";
	public static final String CBOp = "CBOp";
	public static final String RFC = "RFC";
	public static final String RFCp = "RFCp";
	public static final String MPC = "MPC";
	public static final String DAC = "DAC";
	public static final String DACp = "DACp";
	public static final String ICP = "ICP";
	public static final String IHICP = "IHICP";
	public static final String ACAIC = "ACAIC";
	public static final String OCAIC = "OCAIC";
	public static final String DCAEC = "DCAEC";
	public static final String OCAEC = "OCAEC";
	public static final String ACMIC = "ACMIC";
	public static final String OCMIC = "OCMIC";
	public static final String DCMEC = "DCMEC";
	public static final String OCMEC = "OCMEC";
	public static final String AMMIC = "AMMIC";
	public static final String OMMIC = "OMMIC";
	public static final String DMMEC = "DMMEC";
	public static final String OMMEC = "OMMEC";
	
	
	// Inheritance metric
	public static final String DIT = "DIT";
	public static final String NOC = "NOC";
	public static final String AID = "AID";
	public static final String CLD = "CLD";
	public static final String DPA = "DPA";
	public static final String DPD = "DPD";
	public static final String DP = "DP";
	public static final String SPA = "SPA";
	public static final String SPD = "SPD";
	public static final String SP = "SP";
	public static final String NOA = "NOA";
	public static final String NOP = "NOP";
	public static final String NOD = "NOD";
	public static final String SIX = "SIX";
	
	protected static SoftwareMeasureDetailedInformation[] softwareMeasureTable = {
		new SoftwareMeasureDetailedInformation(FILE, MeasureScaleKind.MSK_INTERVAL, "Number of files in given name scope"),
		new SoftwareMeasureDetailedInformation(PKG, MeasureScaleKind.MSK_INTERVAL, "Number of packages in given name scope"),
		new SoftwareMeasureDetailedInformation(CLS, MeasureScaleKind.MSK_INTERVAL, "Number of classes in given name scope"),
		new SoftwareMeasureDetailedInformation(INTF, MeasureScaleKind.MSK_INTERVAL, "Number of interfaces in given name scope"),
		new SoftwareMeasureDetailedInformation(ENUM, MeasureScaleKind.MSK_INTERVAL, "Number of enumerations in given name scope"),
		new SoftwareMeasureDetailedInformation(FLD, MeasureScaleKind.MSK_INTERVAL, "Number of fields in given name scope"),
		new SoftwareMeasureDetailedInformation(MTHD, MeasureScaleKind.MSK_INTERVAL, "Number of methods in given name scope"),
		new SoftwareMeasureDetailedInformation(PARS, MeasureScaleKind.MSK_INTERVAL, "Number of method parameters in given name scope"),
		new SoftwareMeasureDetailedInformation(LOCV, MeasureScaleKind.MSK_INTERVAL, "Number of local variables in given name scope"),

		new SoftwareMeasureDetailedInformation(STMN, MeasureScaleKind.MSK_INTERVAL, "Number of statements in given name scope"),
		new SoftwareMeasureDetailedInformation(LOOPSTMN, MeasureScaleKind.MSK_INTERVAL, "Number of loop statements in given name scope"),
		new SoftwareMeasureDetailedInformation(BRANCHSTMN, MeasureScaleKind.MSK_INTERVAL, "Number of brach statements in given name scope"),
		new SoftwareMeasureDetailedInformation(CFGNODE, MeasureScaleKind.MSK_INTERVAL, "Number of CFG nodes in given name scope"),
		new SoftwareMeasureDetailedInformation(CFGPREDICATE, MeasureScaleKind.MSK_INTERVAL, "Number of predicate CFG nodes in given name scope"),

		new SoftwareMeasureDetailedInformation(LCOM1, MeasureScaleKind.MSK_INTERVAL, 
				"The first metric of Lack of Cohesion in Methods, defined by Chidamber and Kemerer in 1993"),
		new SoftwareMeasureDetailedInformation(LCOM2, MeasureScaleKind.MSK_INTERVAL, 
				"The second metric of Lack of Cohesion in Methods, defined by Chidamber and Kemerer in their 1994 TSE paper"),
		new SoftwareMeasureDetailedInformation(LCOM3, MeasureScaleKind.MSK_INTERVAL,
				"The number of connected components of graph in which the edge between two methods if they use at least one attribute in common"),
		new SoftwareMeasureDetailedInformation(LCOM4, MeasureScaleKind.MSK_INTERVAL,
				"The number of connected components of graph in which the edge between two methods if they use at least one attribute in common or they have invocation relation"),
		new SoftwareMeasureDetailedInformation(LCOM5, MeasureScaleKind.MSK_INTERVAL,
				"Defined by Henderson-sellers using the number of methods which reference an attribute"),
		new SoftwareMeasureDetailedInformation(Co, MeasureScaleKind.MSK_INTERVAL,
				"Connectivity of the graph using in defining LCOM4"),
		new SoftwareMeasureDetailedInformation(CoPrim, MeasureScaleKind.MSK_INTERVAL,
				"A variation on Co"),
		new SoftwareMeasureDetailedInformation(Coh, MeasureScaleKind.MSK_INTERVAL,
				"A variation on LCOM5"),
		new SoftwareMeasureDetailedInformation(TCC, MeasureScaleKind.MSK_INTERVAL,
				"Tight class cohesion"),
		new SoftwareMeasureDetailedInformation(LCC, MeasureScaleKind.MSK_INTERVAL,
				"Loose class cohesion"),
		new SoftwareMeasureDetailedInformation(DCD, MeasureScaleKind.MSK_INTERVAL,
				"Similar to TCC, not only consider using common attributes, but also consider invocation relations"),
		new SoftwareMeasureDetailedInformation(DCI, MeasureScaleKind.MSK_INTERVAL,
				"Similar to LCC, not only consider using common attributes, but also consider invocation relations"),
		new SoftwareMeasureDetailedInformation(OCC, MeasureScaleKind.MSK_INTERVAL,
				"Defined by Amen using the number of methods reachable from a method"),
		new SoftwareMeasureDetailedInformation(CC, MeasureScaleKind.MSK_INTERVAL,
				"Defined by Bonja and Kidanmariam using the similarity between two methods"),
		new SoftwareMeasureDetailedInformation(SCOM, MeasureScaleKind.MSK_INTERVAL,
				"Defined by Fernandez和Pena using the similarity between two methods"),
		new SoftwareMeasureDetailedInformation(LSCC, MeasureScaleKind.MSK_INTERVAL,
				"Defined by Dallal and Briand using the similarity between two methods"),
		new SoftwareMeasureDetailedInformation(ICH, MeasureScaleKind.MSK_INTERVAL,
				"Infomation-flow-based cohesion"),
		new SoftwareMeasureDetailedInformation(CAMC, MeasureScaleKind.MSK_INTERVAL,
				"Cohesion among methods of classes"),
		new SoftwareMeasureDetailedInformation(NHD, MeasureScaleKind.MSK_INTERVAL,
				"Normalised Hamming distance based cohesion"),
		new SoftwareMeasureDetailedInformation(SCC, MeasureScaleKind.MSK_INTERVAL,
				"Similar to LSCC, but can be used in high-level design"),
		new SoftwareMeasureDetailedInformation(CBO, MeasureScaleKind.MSK_INTERVAL,
				"The number of classes which use or is used by the class"),
		new SoftwareMeasureDetailedInformation(CBOi, MeasureScaleKind.MSK_INTERVAL,
				"The number of classes which is used by the class"),
		new SoftwareMeasureDetailedInformation(CBOe, MeasureScaleKind.MSK_INTERVAL,
				"The number of classes which use the class"),
		new SoftwareMeasureDetailedInformation(CBOp, MeasureScaleKind.MSK_INTERVAL,
				"The number of classes without inheritance relationship which use or is used by the class"),
		new SoftwareMeasureDetailedInformation(RFC, MeasureScaleKind.MSK_INTERVAL,
				"The number of methods in the class and directly polymorphically called by the methods in the class"),
		new SoftwareMeasureDetailedInformation(RFCp, MeasureScaleKind.MSK_INTERVAL,
				"The number of methods in the class and directly or indirectly polymorphically called by the methods in the class"),
		new SoftwareMeasureDetailedInformation(MPC, MeasureScaleKind.MSK_INTERVAL,
				"The number of invocations of methods in a class"),
		new SoftwareMeasureDetailedInformation(DAC, MeasureScaleKind.MSK_INTERVAL,
				"The number of attributes in a class have another class as their type"),
		new SoftwareMeasureDetailedInformation(DACp, MeasureScaleKind.MSK_INTERVAL,
				"The number of other classes is used to declare attributes in a class"),
		new SoftwareMeasureDetailedInformation(ICP, MeasureScaleKind.MSK_INTERVAL,
				"The number of method invocations in a class, weighted by the number of parameters of the invoked methods"),
		new SoftwareMeasureDetailedInformation(IHICP, MeasureScaleKind.MSK_INTERVAL,
				"Same as ICP, but counts invocations of methods of ancestors of classes"),
		new SoftwareMeasureDetailedInformation(ACAIC, MeasureScaleKind.MSK_INTERVAL,
				"Class-attribute import coupling with ancestors"),
		new SoftwareMeasureDetailedInformation(OCAIC, MeasureScaleKind.MSK_INTERVAL,
				"Class-attribute import coupling with other classes"),
		new SoftwareMeasureDetailedInformation(DCAEC, MeasureScaleKind.MSK_INTERVAL,
				"Class-attribute export coupling with descendants"),
		new SoftwareMeasureDetailedInformation(OCAEC, MeasureScaleKind.MSK_INTERVAL,
				"Class-attribute export coupling with other classes"),
		new SoftwareMeasureDetailedInformation(ACMIC, MeasureScaleKind.MSK_INTERVAL,
				"Class-method import coupling with ancestors"),
		new SoftwareMeasureDetailedInformation(OCMIC, MeasureScaleKind.MSK_INTERVAL,
				"Class-method import coupling with other classes"),
		new SoftwareMeasureDetailedInformation(DCMEC, MeasureScaleKind.MSK_INTERVAL,
				"Class-method export coupling with descendants"),
		new SoftwareMeasureDetailedInformation(OCMEC, MeasureScaleKind.MSK_INTERVAL,
				"Class-method export coupling with other classes"),
		new SoftwareMeasureDetailedInformation(AMMIC, MeasureScaleKind.MSK_INTERVAL,
				"Method-method import coupling with ancestors"),
		new SoftwareMeasureDetailedInformation(OMMIC, MeasureScaleKind.MSK_INTERVAL,
				"Method-method import coupling with other classes"),
		new SoftwareMeasureDetailedInformation(DMMEC, MeasureScaleKind.MSK_INTERVAL,
				"Method-method export coupling with descendants"),
		new SoftwareMeasureDetailedInformation(OMMEC, MeasureScaleKind.MSK_INTERVAL,
				"Method-method export coupling with other classes"),

		new SoftwareMeasureDetailedInformation(DIT, MeasureScaleKind.MSK_INTERVAL, "Depth of inheritance tree"),
		new SoftwareMeasureDetailedInformation(AID, MeasureScaleKind.MSK_INTERVAL, "Average inheritance depth of a class"),
		new SoftwareMeasureDetailedInformation(CLD, MeasureScaleKind.MSK_INTERVAL, "The maximum number of levels in the heirarchy that are below the class"),
		new SoftwareMeasureDetailedInformation(SIX, MeasureScaleKind.MSK_INTERVAL, "SIX"),
		new SoftwareMeasureDetailedInformation(NOC, MeasureScaleKind.MSK_INTERVAL, "Number of children (including classes and interfaces)"),
		new SoftwareMeasureDetailedInformation(NOP, MeasureScaleKind.MSK_INTERVAL, "Number of parents (including classes and interfaces"),
		new SoftwareMeasureDetailedInformation(NOA, MeasureScaleKind.MSK_INTERVAL, "Number of ancestors (including classes and interfaces)"),
		new SoftwareMeasureDetailedInformation(NOD, MeasureScaleKind.MSK_INTERVAL, "Number of descendants (including classes and interfaces)"),
		new SoftwareMeasureDetailedInformation(SPA, MeasureScaleKind.MSK_INTERVAL, "Static polymorphism in ancestor"),
		new SoftwareMeasureDetailedInformation(SPD, MeasureScaleKind.MSK_INTERVAL, "Static polymorphism in descendants"),
		new SoftwareMeasureDetailedInformation(SP, MeasureScaleKind.MSK_INTERVAL, "SPA+SPD"),
		new SoftwareMeasureDetailedInformation(DPA, MeasureScaleKind.MSK_INTERVAL, "Dynamic polymorphism in ancestor"),
		new SoftwareMeasureDetailedInformation(DPD, MeasureScaleKind.MSK_INTERVAL, "Dynmaic polymorphism in Descendants"),
		new SoftwareMeasureDetailedInformation(DP, MeasureScaleKind.MSK_INTERVAL, "DPA+DPD"),
	};
	
	protected static List<SoftwareMeasureDetailedInformation> extendedTable = new ArrayList<SoftwareMeasureDetailedInformation>();
	
	public static String getDescriptionOfMeasure(String identifier) {
		for (int index = 0; index < extendedTable.size(); index++) {
			if (identifier.equals(extendedTable.get(index).getIdentifier())) 
					return extendedTable.get(index).getDescription();
		}

		for (int index = 0; index < softwareMeasureTable.length; index++) {
			if (identifier.equals(softwareMeasureTable[index].getIdentifier())) 
					return softwareMeasureTable[index].getDescription();
		}
		return identifier;
	}

	public static MeasureScaleKind getScaleOfMeasure(String identifier) {
		for (int index = 0; index < extendedTable.size(); index++) {
			if (identifier.equals(extendedTable.get(index).getIdentifier())) 
					return extendedTable.get(index).getScale();
		}
		for (int index = 0; index < softwareMeasureTable.length; index++) {
			if (identifier.equals(softwareMeasureTable[index].getIdentifier())) 
					return softwareMeasureTable[index].getScale();
		}
		return MeasureScaleKind.MSK_UNKNOWN;
	}
	
	public static String getDescription(SoftwareMeasure measure) {
		return getDescriptionOfMeasure(measure.getIdentifier());
	}

	public static MeasureScaleKind getScale(SoftwareMeasure measure) {
		return getScaleOfMeasure(measure.getIdentifier());
	}
	
	public static boolean hasMeasure(String identifier) {
		for (int index = 0; index < extendedTable.size(); index++) {
			if (identifier.equals(extendedTable.get(index).getIdentifier())) return true;
		}

		for (int index = 0; index < softwareMeasureTable.length; index++) {
			if (identifier.equals(softwareMeasureTable[index].getIdentifier())) return true;
		}
		return false;
	}
	
	public static String[] getAvailableMeasureIdentifiers() {
		if (softwareMeasureTable.length + extendedTable.size() <= 0) return null;
		String[] result = new String[softwareMeasureTable.length + extendedTable.size()];

		int resultIndex = 0;
		for (int index = 0; index < extendedTable.size(); index++) {
			result[resultIndex] = softwareMeasureTable[index].getIdentifier();
			resultIndex++;
		}

		for (int index = 0; index < softwareMeasureTable.length; index++) {
			result[resultIndex] = extendedTable.get(index).getIdentifier();
			resultIndex++;
		}
		
		return result;
	}
	
	public static List<SoftwareMeasure> getAvailableMeasures() {
		ArrayList<SoftwareMeasure> resultList = new ArrayList<SoftwareMeasure>();

		for (int index = 0; index < extendedTable.size(); index++) {
			resultList.add(new SoftwareMeasure(extendedTable.get(index).getIdentifier()));
		}
		for (int index = 0; index < softwareMeasureTable.length; index++) {
			resultList.add(new SoftwareMeasure(softwareMeasureTable[index].getIdentifier()));
		}
		
		return resultList;
	}
	
	public static List<SoftwareMeasure> getAvailableSizeMeasures() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.FILE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.PKG),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.INTF),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ENUM),

				new SoftwareMeasure(SoftwareMeasureIdentifier.ELOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.BLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOPT),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.FLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.PARS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOCV),
				new SoftwareMeasure(SoftwareMeasureIdentifier.STMN),

				new SoftwareMeasure(SoftwareMeasureIdentifier.BYTE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.WORD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CHAR),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}
	
	public static List<SoftwareMeasure> getSelectedSizeMeasures() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.INTF),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ENUM),

				new SoftwareMeasure(SoftwareMeasureIdentifier.ELOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOPT),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.FLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.STMN),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}

	public static List<SoftwareMeasure> getAvailableCohesionMeasures() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM1),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM2),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM3),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM4),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM5),

				new SoftwareMeasure(SoftwareMeasureIdentifier.Co),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CoPrim),
				new SoftwareMeasure(SoftwareMeasureIdentifier.Coh),

				new SoftwareMeasure(SoftwareMeasureIdentifier.TCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCC),

				new SoftwareMeasure(SoftwareMeasureIdentifier.DCD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCI),

				new SoftwareMeasure(SoftwareMeasureIdentifier.OCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.PCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SCOM),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LSCC),

				new SoftwareMeasure(SoftwareMeasureIdentifier.CBMC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ICBMC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.PCCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ICH),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CAMC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SNHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SCC),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}
	
	public static List<SoftwareMeasure> getSelectedCohesionMeasures() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM2),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM5),

				new SoftwareMeasure(SoftwareMeasureIdentifier.TCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SCOM),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LSCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBMC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CAMC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SCC),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}

	public static List<SoftwareMeasure> getAvailableCouplingMeasures() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBO),
				new SoftwareMeasure(SoftwareMeasureIdentifier.RFC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MPC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DAC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ICP),

				new SoftwareMeasure(SoftwareMeasureIdentifier.IHICP),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ACAIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCAIC),

				new SoftwareMeasure(SoftwareMeasureIdentifier.DCAEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCAEC),

				new SoftwareMeasure(SoftwareMeasureIdentifier.ACMIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCMIC),

				new SoftwareMeasure(SoftwareMeasureIdentifier.DCMEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCMEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.AMMIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OMMIC),

				new SoftwareMeasure(SoftwareMeasureIdentifier.DMMEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OMMEC),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}
	
	public static List<SoftwareMeasure> getSelectedCouplingMeasures() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBO),
				new SoftwareMeasure(SoftwareMeasureIdentifier.RFC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MPC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DAC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ICP),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}
	
	public static boolean regisiter(String identifier, String description, MeasureScaleKind scaleKind) {
		if (hasMeasure(identifier)) return true;
		return extendedTable.add(new SoftwareMeasureDetailedInformation(identifier, scaleKind, description));
	}
}

class SoftwareMeasureDetailedInformation {
	String identifier = null;
	String description = null;
	MeasureScaleKind scale = MeasureScaleKind.MSK_INTERVAL;
	
	SoftwareMeasureDetailedInformation(String identifier, MeasureScaleKind scale, String description) {
		this.identifier = identifier;
		this.description = description;
		this.scale = scale;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getDescription() {
		return description;
	}

	public MeasureScaleKind getScale() {
		return scale;
	}
}
