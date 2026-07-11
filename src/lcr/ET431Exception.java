package lcr;

/**
 * Custom exception representing hardware communication errors, parsing failures,
 * or command transmission timeout exceptions specific to the East Tester ET431
 * LCR meter control layer.
 * * @author sylkat
 */
public class ET431Exception extends Exception {

    /**
     * Constructs a new ET431Exception with the specified descriptive error message detail payload.
     * * @param message the detailed breakdown message context tracking the root cause of the exception
     */
    public ET431Exception(String message) {
        super(message);
    }
}