package ldif.local.datasources.dump.parser;

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10.06.11
 * Time: 16:29
 * To change this template use File | Settings | File Templates.
 */

public class ParseException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(String message) {
		super(message);
	}

}