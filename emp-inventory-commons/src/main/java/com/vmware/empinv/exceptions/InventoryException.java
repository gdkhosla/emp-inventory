package com.vmware.empinv.exceptions;

public class InventoryException extends Exception {
	private static final long serialVersionUID = 1L;

	public InventoryException() {
		super();
	}

	public InventoryException(String message) {
		super(message);
	}
}
