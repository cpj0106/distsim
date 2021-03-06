package edu.unlv.cs.edas.user.domain;

public class UserAlreadyExistsException extends Exception {

	private static final long serialVersionUID = -34934599161720953L;

	public UserAlreadyExistsException() {
		super();
	}

	public UserAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserAlreadyExistsException(String message) {
		super(message);
	}

	public UserAlreadyExistsException(Throwable cause) {
		super(cause);
	}
	
}
