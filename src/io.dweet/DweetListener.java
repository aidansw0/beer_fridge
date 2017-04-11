package io.dweet;

/**
 * DweetListener listens to dweets from things.
 * 
 * @author Khaled Bakhit <kb@khaled-bakhit.com>
 */
public interface DweetListener 
{
    /**
     * Indication that a dweet has been received.
     * @param dweet Received dweet from the thing this listener was listening to.
     */
    public void dweetReceived(Dweet dweet);
    /**
     * Stop listening to a thing. Should affect the return of {@link #isStillListeningTo(java.lang.String)}.
     * Once a listener stops listening to a thing, it will call {@link #stoppedListening(java.lang.String)}.
     * @param thingName Name of the thing to stop listening to.
     */
    public void stopListening(String thingName);
    /**
     * Check if still listening to a thing.
     * @param thingName Name of thing to check.
     * @return True if still listening, false otherwise.
     */
    public boolean isStillListeningTo(String thingName);
    /**
     * Indication that listening to a thing's stream has been stopped.
     * @param thingName Name of thing this listener is no longer listening to.
     */
    public void stoppedListening(String thingName);    
    /**
     * Indication that connection has been lost to a thing's stream.
     * @param thingName Name of thing this listener is no longer listening to.
     * @param e the Exception that caused this problem.
     */
    public void connectionLost(String thingName, Exception e);
    
}
