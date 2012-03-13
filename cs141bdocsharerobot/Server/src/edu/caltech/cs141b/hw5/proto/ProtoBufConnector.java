package edu.caltech.cs141b.hw5.proto;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import edu.caltech.cs141b.hw5.data.CollabMessages.RequestMessage;
import edu.caltech.cs141b.hw5.data.CollabMessages.ResponseMessage;

/**
 * Helper class for setting up HTTP connection between the client and the server.
 * 
 * @author aliu
 *
 */
public class ProtoBufConnector {
	private static String DO_POST = "POST";
	//private static String DO_GET = "GET";
	
	private String address;
	
	public ProtoBufConnector(String address) {
		this.address = address;
	}
	
	/**
	 * Set up connection parameters, make the request, and parse the results for 
	 * return
	 * 
	 * @param input
	 * @return response
	 */
	public ResponseMessage makePostConnect(RequestMessage input) {
		ResponseMessage result = null;
		try {
			URL url = new URL(address);	
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setDoOutput(true);
			conn.setReadTimeout(10000);
			conn.setRequestProperty("Content-Type", "application/x-protobuf");
			conn.setRequestProperty("Content-Length", 
					Integer.toString(input.getSerializedSize()));
			conn.setRequestMethod(DO_POST);
			input.writeTo(conn.getOutputStream());
			conn.connect();	
			result = ResponseMessage.parseFrom(conn.getInputStream());
		} catch (MalformedURLException e) {
			System.err.println("Url " + address + " is invalid.");
		} catch (ProtocolException e) {
				System.err.println("Can't set the http request method to " + DO_POST);
		} catch (IOException e) {
			System.err.println("Can't write RequestMessage to connection.");
		}
		return result;
	}
}
