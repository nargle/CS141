
public class Message {
    /**
     * Token type (request or message).
     */
    private String type;
    
    /**
     * Client who sent the message
     */
    private Client client;
    
    /**
     * Default constructor, initializes the message
     * @param c Client who sent the message
     * @param t Message type (request or termination)
     */
    public Message(Client c, String t) {
        client = c;
        type = t;
    }
    
    /**
     * Returns the Message type
     * @return The Message type (request or termination)
     */
    public String getType() {
        return type;
    }
    
    /**
     * Returns the Client who sent the message
     * @return The Client who sent the message
     */
    public Client getClient() {
        return client;
    }
    
    /**
     * Returns the ID of the Client who sent the message
     * @return The ID of the Client who sent the message
     */
    public int getClientID() {
        return client.getID();
    }
    
    
}
