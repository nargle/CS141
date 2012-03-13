package edu.caltech.cs141b.hw5.android;

import edu.caltech.cs141b.hw5.android.data.InvalidRequest;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DocShow extends Activity {
	
	private static final int DOC_EDIT = 1;
	private TextView myTitleText;
	private TextView myContentsText;
	private String myKey;
	private Button lockButton;
	private CollabServiceWrapper service;
	private UnlockedDocument myDoc;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showdoc);
        service = new CollabServiceWrapper();
        
        //Set up references
        myTitleText = (TextView) findViewById(R.id.title_field);
        myContentsText = (TextView) findViewById(R.id.contents_field);
        lockButton = (Button) findViewById(R.id.lock_button);

        //Initialize values to false or null    
        myKey = null;
        myDoc = null;
        
        //There must be an id for this activity to have been started
        Bundle extras = getIntent().getExtras();
        
        if(extras != null)
        {
        	myKey = extras.getString("docKey");
        }
        else
        {
    		//Notify user that there was something wrong
    		Intent resultIntent = new Intent();
    		resultIntent.putExtra("toastText", "Hmmm... Didn't get a key");
    		setResult(Activity.RESULT_CANCELED, resultIntent);
        	finish();
        }
        
		try
		{
			myDoc = service.getDocument(myKey);
	        
	        //Populate the fields with the item information
	        populateFields();
	        
	        lockButton.setOnClickListener(new View.OnClickListener() {
	        	public void onClick(View v) {
	            	//Starts the DocActivity activity, which will be populated with default fields
	            	Intent i = new Intent(DocShow.this, DocActivity.class);
	            	i.putExtra("docKey", DocShow.this.myKey);
	            	startActivityForResult(i, DocShow.DOC_EDIT);
	        	}
			});
		}
		catch (InvalidRequest e) {
    		//Notify user that there was an invalid request
    		Intent resultIntent = new Intent();
    		resultIntent.putExtra("toastText", "That was an invalid request!");
    		setResult(Activity.RESULT_CANCELED, resultIntent);
        	finish();
       }
    }
    
    //This method populates the TextViews and Buttons, etc. for the user to see
    private void populateFields()
    {
    	if(myKey != null)
    	{
			myTitleText.setText(myDoc.getTitle());
			myContentsText.setText(myDoc.getContents().replace("<br>", "\n"));
    	}
    	else
    	{
    		//Notify user that there was an invalid request
    		Intent resultIntent = new Intent();
    		resultIntent.putExtra("toastText", "That's weird... I didn't get a document key!");
    		setResult(Activity.RESULT_CANCELED, resultIntent);
    		finish();
    	}
    }    
    
    protected void onActivityResult(int reqCode, int resCode, Intent i)
    {
    	//When an activity finishes, this method is called
    	super.onActivityResult(reqCode, resCode, i);
    	
    	if(i != null && i.hasExtra("toastText") && i.getStringExtra("toastText") != null)
    	{
        	Toast.makeText(this, i.getStringExtra("toastText"), Toast.LENGTH_LONG).show();
    	}
    	
    	//Update the fields, in case user changed title/contents
		try
		{
			myDoc = service.getDocument(myKey);
			populateFields();
		}
		catch (InvalidRequest e) {
    		//Notify user that there was an invalid request
    		Intent resultIntent = new Intent();
    		resultIntent.putExtra("toastText", "That was an invalid request!");
    		setResult(Activity.RESULT_CANCELED, resultIntent);
        	finish();
        }
    }
}