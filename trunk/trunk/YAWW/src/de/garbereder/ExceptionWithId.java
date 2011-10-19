package de.garbereder;

public class ExceptionWithId extends Exception {

	private static final long serialVersionUID = 5115404540571090549L;
	private int id;
	
	public ExceptionWithId(int id)
	{
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
