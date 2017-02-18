package ee.ttu.webapps.journal;

public class JournalException extends Exception {

	
	private static final long serialVersionUID = 7424975049020814223L;
	
	public JournalException() {
		
	}
	
	public JournalException(String msg) {
		super(msg);
	}
	
	public JournalException(String e, Throwable t) {
		super(e, t);
	}
	
}
