package measurement.metric.size;

import java.util.List;
import java.util.Scanner;

import measurement.measure.SoftwareMeasure;
import measurement.measure.SoftwareMeasureIdentifier;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import softwareStructure.SoftwareStructManager;
import sourceCodeAST.SourceCodeFile;

/**
 * @author Zhou Xiaocong
 * @since 2015年7月9日
 * @version 1.0
 */
public class CodeLineCounterMetric extends SoftwareSizeMetric {
	private CodeLineCounter counter = new CodeLineCounter();
	private int ELOCNumber = 0;
	private int CLOCNumber = 0;
	private int BLOCNumber = 0;
	private int LOPTNumber = 0;
	private int NLOCNumber = 0;
	private int wordNumber = 0;
	private int charNumber = 0;
	private int byteNumber = 0;
	
	@Override
	public void setSoftwareStructManager(SoftwareStructManager structManager) {
		if (structManager != this.structManager) {
			this.structManager = structManager;
			tableManager = structManager.getNameTableManager();
			parser = tableManager.getSouceCodeFileSet();
			
			reset();
			counter.reset();
		}
	}

	@Override
	public void setMeasuringObject(NameScope objectScope) {
		if (this.objectScope != objectScope) {
			this.objectScope = objectScope;
			
			reset();
			counter.reset();
		}
	}

	private void reset() {
		ELOCNumber = 0;
		CLOCNumber = 0;
		BLOCNumber = 0;
		NLOCNumber = 0;
		LOPTNumber = 0;
		wordNumber = 0;
		charNumber = 0;	
		byteNumber = 0;
	}
	
	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (tableManager == null || objectScope == null) return false;

		if (LOPTNumber <= 0) {
			NameScopeKind kind = objectScope.getScopeKind();
			if (kind == NameScopeKind.NSK_SYSTEM) {
				counterSystemSourceCode();
			} else if (kind == NameScopeKind.NSK_COMPILATION_UNIT) {
				CompilationUnitScope unitScope = (CompilationUnitScope)objectScope;
				counterCompilationUnitSourceCode(unitScope);

				ELOCNumber = counter.getELOCNumber();
				CLOCNumber = counter.getCLOCNumber();
				BLOCNumber = counter.getBLOCNumber();
				NLOCNumber = counter.getNLOCNumber();
				LOPTNumber = counter.getLOPTNumber();
				wordNumber = counter.getWordNumber();
				charNumber = counter.getCharNumber();	
				byteNumber = counter.getByteNumber();
			} else if (kind == NameScopeKind.NSK_PACKAGE) {
				PackageDefinition packageDefinition = (PackageDefinition)objectScope;
				counterPackageSourceCode(packageDefinition);
			} else if (kind == NameScopeKind.NSK_DETAILED_TYPE) {
				DetailedTypeDefinition typeDefinition = (DetailedTypeDefinition)objectScope;
				counterClassSourceCode(typeDefinition);

				ELOCNumber = counter.getELOCNumber();
				CLOCNumber = counter.getCLOCNumber();
				BLOCNumber = counter.getBLOCNumber();
				NLOCNumber = counter.getNLOCNumber();
				LOPTNumber = counter.getLOPTNumber();
				wordNumber = counter.getWordNumber();
				charNumber = counter.getCharNumber();	
				byteNumber = counter.getByteNumber();
			} else if (kind == NameScopeKind.NSK_METHOD) {
				MethodDefinition methodDefinition = (MethodDefinition)objectScope;
				counterMethodSourceCode(methodDefinition);

				ELOCNumber = counter.getELOCNumber();
				CLOCNumber = counter.getCLOCNumber();
				BLOCNumber = counter.getBLOCNumber();
				NLOCNumber = counter.getNLOCNumber();
				LOPTNumber = counter.getLOPTNumber();
				wordNumber = counter.getWordNumber();
				charNumber = counter.getCharNumber();	
				byteNumber = counter.getByteNumber();
			}
		}
		
		if (measure.match(SoftwareMeasureIdentifier.ELOC)) measure.setValue(ELOCNumber);
		else if (measure.match(SoftwareMeasureIdentifier.CLOC)) measure.setValue(CLOCNumber);
		else if (measure.match(SoftwareMeasureIdentifier.BLOC)) measure.setValue(BLOCNumber);
		else if (measure.match(SoftwareMeasureIdentifier.NLOC)) measure.setValue(NLOCNumber);
		else if (measure.match(SoftwareMeasureIdentifier.LOPT)) measure.setValue(LOPTNumber);
		else if (measure.match(SoftwareMeasureIdentifier.WORD)) measure.setValue(wordNumber);
		else if (measure.match(SoftwareMeasureIdentifier.CHAR)) measure.setValue(charNumber);
		else if (measure.match(SoftwareMeasureIdentifier.BYTE)) measure.setValue(byteNumber);
		else if (measure.match(SoftwareMeasureIdentifier.LOC)) measure.setValue(ELOCNumber+CLOCNumber+BLOCNumber+NLOCNumber);
		else return false;
		
		return true;
	}
	
	protected void counterSystemSourceCode() {
		List<CompilationUnitScope> unitList = tableManager.getAllCompilationUnitScopes();
		if (unitList == null) return;
		
		for (CompilationUnitScope unit : unitList) {
			counter.reset();
			counterCompilationUnitSourceCode(unit);

			ELOCNumber += counter.getELOCNumber();
			CLOCNumber += counter.getCLOCNumber();
			BLOCNumber += counter.getBLOCNumber();
			NLOCNumber += counter.getNLOCNumber();
			LOPTNumber += counter.getLOPTNumber();
			wordNumber += counter.getWordNumber();
			charNumber += counter.getCharNumber();	
			byteNumber += counter.getByteNumber();
		}
	}

	protected void counterCompilationUnitSourceCode(CompilationUnitScope unit) {
		SourceCodeFile codeFile = parser.findSourceCodeFileByFileUnitName(unit.getUnitName());
		String content = codeFile.getFileContent();
		if (content == null) return;
		
		int totalLines = codeFile.getTotalLines();
		counter.counter(content, 1, totalLines+1);
		
		// Release the memory in the parser. When can findDeclarationForCompilationUnitScope, it will load 
		// file contents and AST to the memory.
		codeFile.releaseFileContent();
	}

	protected void counterPackageSourceCode(PackageDefinition packageDefinition) {
		List<CompilationUnitScope> unitList = packageDefinition.getCompilationUnitScopeList();
		if (unitList == null) return;

		for (CompilationUnitScope unit : unitList) {
			counter.reset();
			counterCompilationUnitSourceCode(unit);

			ELOCNumber += counter.getELOCNumber();
			CLOCNumber += counter.getCLOCNumber();
			BLOCNumber += counter.getBLOCNumber();
			NLOCNumber += counter.getNLOCNumber();
			LOPTNumber += counter.getLOPTNumber();
			wordNumber += counter.getWordNumber();
			charNumber += counter.getCharNumber();	
			byteNumber += counter.getByteNumber();
		}
	}

	protected void counterClassSourceCode(DetailedTypeDefinition type) {
		CompilationUnitScope unit = tableManager.getEnclosingCompilationUnitScope(type);
		SourceCodeFile codeFile = parser.findSourceCodeFileByFileUnitName(unit.getUnitName());

		String content = codeFile.getFileContent();
		if (content == null) return;
		
		int startLine = type.getLocation().getLineNumber();
		int endLine = type.getEndLocation().getLineNumber() + 1;

		counter.counter(content, startLine, endLine);
		codeFile.releaseFileContent();
	}
	
	protected void counterMethodSourceCode(MethodDefinition method) {
		CompilationUnitScope unit = tableManager.getEnclosingCompilationUnitScope(method);
		SourceCodeFile codeFile = parser.findSourceCodeFileByFileUnitName(unit.getUnitName());

		String content = codeFile.getFileContent();
		if (content == null) return;
		
		int startLine = method.getLocation().getLineNumber();
		int endLine = method.getEndLocation().getLineNumber() + 1;

		counter.counter(content, startLine, endLine);
		codeFile.releaseFileContent();
	}

}


class CodeLineCounter {
	private int ELOCNumber = 0;
	private int CLOCNumber = 0;
	private int BLOCNumber = 0;
	private int LOPTNumber = 0;
	private int NLOCNumber = 0;
	private int wordNumber = 0;
	private int charNumber = 0;
	private int byteNumber = 0;
	
	public void reset() {
		ELOCNumber = 0;
		CLOCNumber = 0;
		BLOCNumber = 0;
		NLOCNumber = 0;
		LOPTNumber = 0;
		wordNumber = 0;
		charNumber = 0;	
		byteNumber = 0;
	}

	public int getELOCNumber() {
		return ELOCNumber;
	}

	public int getCLOCNumber() {
		return CLOCNumber;
	}

	public int getBLOCNumber() {
		return BLOCNumber;
	}

	public int getNLOCNumber() {
		return NLOCNumber;
	}

	public int getLOPTNumber() {
		return LOPTNumber;
	}

	public int getWordNumber() {
		return wordNumber;
	}

	public int getCharNumber() {
		return charNumber;
	}
	
	public int getByteNumber() {
		return byteNumber;
	}
	
	/**
	 * Counter various number (i.e. defined as the fields in the class) in the content, starting with the startLine (include the startLine), 
	 * and ending with the endLine (NOT include the endLine). 
	 */
	public void counter(String content, int startLine, int endLine) {
		if (content == null) return;
		if (startLine < 1) return;
		if (endLine <= startLine) return;
		
		try {
			final Scanner scanner = new Scanner(content);
			int lineNumber = 0;
			boolean nextLineInComments = false;
			String line = null;
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				lineNumber++;
				
				if (lineNumber >= startLine && lineNumber < endLine) {
					LOPTNumber++;
					byteNumber += line.length();
					nextLineInComments = counterLine(line, nextLineInComments);
				} else if (lineNumber >= endLine) {
					scanner.close();
					return;
				}
			}
			scanner.close();
		} catch (Exception exc) {
			reset();
			return;
		}
	}
	
	/**
	 * Counter various number (i.e. defined as the fields in the class) for a line in the content. 
	 * The boolean parameter nextLineInComments indicates if the scanning line is in comment or not, and also, 
	 * after scanning the given line, we must to determine the next line is also in comment or not, i.e, if the given line
	 * include a comment end string (i.e. "* /") or not.
	 */ 
	public boolean counterLine(String line, boolean nextLineInComments) {
		boolean inString = false;
		boolean hasCode = false;
		boolean inLineComment = false;
		boolean inWord = false;
		boolean isEmpty = true;
		boolean hasComment = false;

		int index = 0;
		int length = line.length();

		while (index < length) {
			// Skip all space character 
			char ch = line.charAt(index);
			index = index + 1;

			char nextChar = '\n';
			if (index < length) nextChar = line.charAt(index); 
			
			if (nextLineInComments) {
				hasComment = true;
				isEmpty = false;		// We do not calculate the empty line in comments as an empty line.
				
				if (ch == '*' && nextChar == '/') {
					nextLineInComments = false;
					index = index + 1;
				} 
			} else if (!inLineComment) {
				if (ch == '\"') {
					if (inString == true) inString = false; else inString = true;
					hasCode = true;
				} else if (ch == '/' && nextChar == '/' && !inString) {
					hasComment = true;
					inLineComment = true;
					index = index + 1;
				} else if (ch == '/' && nextChar == '*' && !inString) {
					nextLineInComments = true;
					hasComment = true;
					index = index + 1;
				} else if (ch == '@' && !inString) {
					NLOCNumber++;
					isEmpty = false;
					// Assume that all characters after this mark belong to the notation! 
					break;
				}
				
				if (!inLineComment && !nextLineInComments) {
					if (!Character.isWhitespace(ch)) {
						isEmpty = false;
						hasCode = true;
					}
					if (!inString) {
						if (isTokenChar(ch, inWord)) {
							if (!inWord) inWord = true;
							charNumber++;
						} else {
							if (inWord) {
								wordNumber++;
							}
							inWord = false;
						}
					} 
				}
			} else {
				isEmpty = false; 	// If the line is in line comments, it is not an empty line.
			}
		} 
		
		if (hasComment) CLOCNumber++;
		if (hasCode) ELOCNumber++;
		if (isEmpty) BLOCNumber++;
		return nextLineInComments;   
	}
	
	private boolean isTokenChar(char ch, boolean inWord) {
		if (inWord) return Character.isJavaIdentifierPart(ch);
		else return Character.isJavaIdentifierStart(ch);
	}
	
}
