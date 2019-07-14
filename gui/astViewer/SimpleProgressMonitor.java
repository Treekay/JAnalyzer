package gui.astViewer;

import javax.swing.JFrame;
import javax.swing.ProgressMonitor;

import org.eclipse.core.runtime.IProgressMonitor;

public class SimpleProgressMonitor implements IProgressMonitor {
	private ProgressMonitor progressDialog = null;
	private JFrame parent = null;
	private boolean isCanceled = false;

	public SimpleProgressMonitor(JFrame parent) {
		this.parent = parent;
	} 
	
	public void beginTask(String name, int totalWork) {
		progressDialog = new ProgressMonitor(parent, name, null, 0, totalWork);
	}

	public void done() {
		progressDialog.close();
	}

	public void internalWorked(double work) {
	}

	public boolean isCanceled() {
		isCanceled = progressDialog.isCanceled();
		return isCanceled;
	}

	public void setCanceled(boolean value) {

	}

	public void setTaskName(String name) {
	}

	public void subTask(String name) {
		progressDialog.setNote(name);
	}

	public void worked(int work) {
		progressDialog.setProgress(work);
	}

}
