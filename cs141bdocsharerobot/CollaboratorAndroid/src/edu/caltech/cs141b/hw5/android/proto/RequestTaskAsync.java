package edu.caltech.cs141b.hw5.android.proto;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;
import edu.caltech.cs141b.hw5.android.data.CollabMessages.RequestMessage;
import edu.caltech.cs141b.hw5.android.data.CollabMessages.ResponseMessage;

/**
 * Threaded implementation of making HTTP connection with protocol buffer
 * payload.
 * 
 * @author aliu
 *
 */
public class RequestTaskAsync extends AsyncTask<RequestMessage, String, ResponseMessage> {

	private static String TAG = "RequestTask";
	private static String DO_POST = "POST";
	
	/*
	 * In TEST mode: replace the hostname with your internal IP address 
	 * e.g. 192.168.1.% for ethernet and test it in an emulator
	 * 
	 * In DEVELOPMENT mode: replace the hostname with your app engine hostname.
	 */	
	/*private static final String address = "http://192.168.1.8:8888/collab/protoc"; */
	private static final String address = "http://cs141bdocsharerobot.appspot.com/collab/protoc"; 

	
    @Override
    protected ResponseMessage doInBackground(RequestMessage... inputs) {	

    	RequestMessage input = inputs[0];
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
			Log.e(TAG, "Url " + address + " is invalid.");
		} catch (ProtocolException e) {
			Log.e(TAG, "Can't set the http request method to " + DO_POST);
		} catch (IOException e) {
			Log.e(TAG, "Can't write RequestMessage to connection.");
			e.printStackTrace();
		} finally {
			
		}
		return result;
    }
    
    @Override
    protected void onProgressUpdate(String... progress) {
    }

    @Override
    protected void onPostExecute(ResponseMessage result) {
        super.onPostExecute(result);
    }
}
