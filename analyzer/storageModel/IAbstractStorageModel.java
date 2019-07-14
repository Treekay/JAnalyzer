package analyzer.storageModel;

import nameTable.nameDefinition.NameDefinition;
import sourceCodeAST.SourceCodeLocation;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê4ÔÂ10ÈÕ
 * @version 1.0
 *
 */
public interface IAbstractStorageModel {
	/**
	 * Test if the current instance of the storage model refer to the same storage of another instance of 
	 * the storage model 
	 */
	public boolean referToSameStorage(IAbstractStorageModel another);
	
	/**
	 * Test if the storage of the current instance is accessible in the give source code location 
	 */
	public boolean accessible(SourceCodeLocation location);
	
	/**
	 * Return the reference expression of the storage model  
	 */
	public String getExpression();
	
	public boolean isPrimitive();
	
	public StorageModelKind getKind();
	
	public NameDefinition getCoreDefinition();
}
