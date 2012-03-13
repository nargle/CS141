package edu.caltech.cs141b.hw5.android;

import java.util.Date;

import edu.caltech.cs141b.hw5.android.data.InvalidRequest;
import edu.caltech.cs141b.hw5.android.data.LockExpired;
import edu.caltech.cs141b.hw5.android.data.LockUnavailable;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DocActivity extends Activity {
	
	private EditText myTitleText;
	private EditText myContentsText;
	private String myKey;
	private Button createButton;
	private boolean saveButtonHit;
	private CollabServiceWrapper service;
	private LockedDocument myDoc;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.docsheet);
        
        service = new CollabServiceWrapper();
        
        //Set up references
        myTitleText = (EditText) findViewById(R.id.title_field);
        myContentsText = (EditText) findViewById(R.id.contents_field);
        createButton = (Button) findViewById(R.id.accept_button);
        
        myContentsText.setMinLines(5);
        
        //Initialize values to false and null
        saveButtonHit = false;
        myKey = null;
        myDoc = null;

        Bundle extras = getIntent().getExtras();
        
        if(extras != null)
        {
        	myKey = (String)extras.getString("docKey");
        	//Requests the lock for the document
        	try
        	{
        		myDoc = service.lockDocument(myKey);        		
                
                //Populate the fields
                populateFields();
        	}
        	catch (LockUnavailable e)
        	{
        		//Notify user that lock is unavailable
        		Intent resultIntent = new Intent();
        		resultIntent.putExtra("toastText", "Sorry, that document is already locked!");
        		setResult(Activity.RESULT_CANCELED, resultIntent);
            	finish();
        	}
        	catch (InvalidRequest e)
        	{
        		//Notify user that there was an invalid request
        		Intent resultIntent = new Intent();
        		resultIntent.putExtra("toastText", "That was an invalid request!");
        		setResult(Activity.RESULT_CANCELED, resultIntent);
            	finish();
        	}
        }
        
        //Set up the OnClickListeners for the create button  
        createButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		if(myTitleText.getText().length() == 0)
        		{
        			showNeedNameDialog();   			
        		}
        		else
        		{
            		saveButtonHit = true;
        			saveState();
            		setResult(RESULT_OK);
            		finish();        			
        		}
        	}
        });
    }
    
    //If the user tries to create the item without a name, display an AlertDialog.Builder
    private void showNeedNameDialog()
    {
    	AlertDialog.Builder itemAlert = new AlertDialog.Builder(this);
    	itemAlert.setMessage("I'm sorry, your doc needs a name before it can be created.");
    	itemAlert.setTitle("Need Doc's Name!");
    	itemAlert.setNeutralButton("Return", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface arg0, int arg1) {
    		}
    	});
    	itemAlert.show();
    }
    
    //This method populates the fields with the current item's information saved in the database
    //If there is no id set, then the fields are all at their default values
    private void populateFields()
    {
		if (myKey != null && myDoc != null)
		{
			myTitleText.setText(myDoc.getTitle());
			myContentsText.setText(myDoc.getContents().replace("<br>", "\n"));
		}
    }    
    
    //This method saves the information of the doc, checking that the lock
    //has not expired yet
    private void saveState() {
    	String title = myTitleText.getText().toString();
    	String contents = myContentsText.getText().toString();
    	
    	if (myDoc != null)
    	{
    		myDoc.setTitle(title);
    		myDoc.setContents(contents.replace("\n", "<br>"));
    	}
    	else
    	{
    		myDoc = new LockedDocument(null, null, null, title, contents);
    	}
    	
    	try {
			service.saveDocument(myDoc);
			//Notify user save successful
    		Intent resultIntent = new Intent();
    		resultIntent.putExtra("toastText", "Document saved.");
    		setResult(Activity.RESULT_OK, resultIntent);
        	finish();	
			setResult(Activity.RESULT_OK, resultIntent);
		} catch (LockExpired e) {
    		//Notify user that the lock expired
    		Intent resultIntent = new Intent();
    		resultIntent.putExtra("toastText", "I'm sorry, your lock has expired!");
    		setResult(Activity.RESULT_CANCELED, resultIntent);
        	finish();		
		} catch (InvalidRequest e) {
    		//Notify user that there was an invalid request
    		Intent resultIntent = new Intent();
    		resultIntent.putExtra("toastText", "That was an invalid request!");
    		setResult(Activity.RESULT_CANCELED, resultIntent);
        	finish();
		}
    }
    
    public void finish()
    {
    	//isFinishing = true;
    	//saveState();
    	if(!saveButtonHit)
    	{
	    	try {
				if(myDoc != null)
					service.releaseLock(myDoc);
			} catch (LockExpired e) {
	    		//Notify user that the lock expired
	    		Intent resultIntent = new Intent();
	    		resultIntent.putExtra("toastText", "I'm sorry, your lock has expired!");
	    		setResult(Activity.RESULT_CANCELED, resultIntent);			
			} catch (InvalidRequest e) {
        		//Notify user that there was an invalid request
        		Intent resultIntent = new Intent();
        		resultIntent.putExtra("toastText", "That was an invalid request!");
        		setResult(Activity.RESULT_CANCELED, resultIntent);
			}
    	}
    	super.finish();
    }
}