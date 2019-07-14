package analyzer.cfg;

import graph.cfg.ExecutionPoint;

/**
 * @author Zhou Xiaocong
 * @since 2018Äê7ÔÂ19ÈÕ
 * @version 1.0
 *
 */
public class ExecutionPointWorkingList {
	protected ListNode head = null;
	
	public boolean add(ExecutionPoint node) {
		if (head == null) {
			ListNode newNode = new ListNode();
			newNode.node = node;
			newNode.next = null;
			head = newNode;
			return true;
		}

		ListNode current = head;
		ListNode prev = null;
		while (current != null) {
			if (current.node == node) return false;
			if (current.node.compareTo(node) > 0) break; 
			prev = current;
			current = current.next;
		}
		ListNode newNode = new ListNode();
		newNode.node = node;
		newNode.next = current;
		if (prev == null) head = newNode;
		else prev.next = newNode;
		return true;
	}
	
	public ExecutionPoint removeFirst() {
		if (head == null) return null;
		ExecutionPoint node = head.node;
		head = head.next;
		return node;
	}
	
	public boolean isEmpty() {
		return head == null;
	}
	
	public boolean contains(ExecutionPoint node) {
		ListNode current = head;
		while (current != null) {
			if (current.node == node) return true;
			current = current.next;
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		ListNode current = head;
		while (current != null) {
			if (current == head) builder.append("(" + current.node.getId() + ")");
			else builder.append(", (" + current.node.getId() + ")");
			current = current.next;
		}
		builder.append("]");
		return builder.toString();
	}
}

class ListNode {
	ExecutionPoint node = null;
	ListNode next = null;
}