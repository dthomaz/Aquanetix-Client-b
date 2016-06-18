package uk.co.aquanetix.network;

/**
 * A generic callback for all asynchronous operations.
 */
public interface OnResult<T> {
    
	public void getResult(T result);
	
}
