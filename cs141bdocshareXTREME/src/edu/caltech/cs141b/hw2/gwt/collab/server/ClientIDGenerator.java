package edu.caltech.cs141b.hw2.gwt.collab.server;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gwt.user.client.rpc.IsSerializable;


/**
 * The server side implementation of the RPC service.
 */
@PersistenceCapable
public class ClientIDGenerator implements IsSerializable {
    @PrimaryKey
    @Persistent
    private String key = "ClientIDGenerator";
 
    @Persistent
    private int clientID;
 
    public ClientIDGenerator() {
        clientID = 0;
    }
 
    public String generateClientID() {
        clientID++;
        return ((Integer)clientID).toString();
    }
}