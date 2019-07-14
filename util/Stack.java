package util;

import java.util.ArrayList;

/**
 * @author Zhou Xiaocong
 * @since 2013-2-23
 * @version 1.0
 */
public class Stack<T> {
	private ArrayList<T> data = new ArrayList<T>();
	private int currentIndex = 0;
	private boolean canPushNull = false;
	
	public Stack() {
	}

	public Stack(boolean canPushNull) {
		this.canPushNull = canPushNull;
	}
	
	public void push(T obj) {
		if (!canPushNull && obj == null) 
			throw new AssertionError("Try to push null object to the stack in index " + currentIndex + "!");

		data.add(currentIndex, obj);
		currentIndex = currentIndex + 1;
	}
	
	public void pop() {
		if (currentIndex < 1) return;

		data.remove(currentIndex - 1);
		currentIndex = currentIndex - 1;
	}
	
	@SuppressWarnings("null")
	public T getTop() {
		if (currentIndex < 1) return null;
		return data.get(currentIndex - 1);
	}
	
	public boolean isEmpty() {
		return (currentIndex == 0);
	}
	
	public void clear() {
		data.clear();
		currentIndex = 0;
	}
	
}
