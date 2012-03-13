package edu.caltech.cs141b.hw5.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import edu.caltech.cs141b.hw5.android.data.DocumentMetadata;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class CollaboratorAndroidActivity extends ListActivity /*implements OnClickListener*/ {	
	private ArrayAdapter<String> currViewAdapter;
	//The currListInfo HashMap contains the documents' positions in the
	//ListView, mapped to their keys
	private HashMap<Integer, String> currListInfo;
	private String currKey;
	private Button newDoc;
	private Button refreshDocs;
	private static final int DOC_ADD = 0;
	private static final int DOC_EDIT = 1;
	private static final int DOC_SHOW = 2;
	private SharedPreferences prefs;
	private CollabServiceWrapper service;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	checkForEULA();
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    	service = new CollabServiceWrapper();      

        registerForContextMenu(getListView());
        
        newDoc = (Button) findViewById(R.id.new_doc);
        newDoc.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
            	//This method is called when the "New Document" button is clicked
            	//It starts the DocActivity activity, which will be populated with default fields
            	Intent i = new Intent(CollaboratorAndroidActivity.this, DocActivity.class);
            	startActivityForResult(i, CollaboratorAndroidActivity.DOC_ADD);
        	}
        });
        
        refreshDocs = (Button) findViewById(R.id.refresh_docs);
        refreshDocs.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		CollaboratorAndroidActivity.this.fillDocList();
        	}
        });
        
        currKey = null; // Not used until a doc in the ListView is clicked
        fillDocList(); // Sets up the adapter for currView
    }
    
    private void checkForEULA()
    {
    	//This method checks whether the user has accepted the End User License Agreement
    	//If they have, nothing happens, and the program runs normally
    	//If they haven't, the EULA is displayed and must be accepted before the app will run
    	prefs = this.getSharedPreferences("EULA", Activity.MODE_PRIVATE);
    	if(!prefs.getBoolean("AcceptedEULA", false))
    	{	
    		AlertDialog.Builder dispEULA = new AlertDialog.Builder(this);
    		dispEULA.setCancelable(false);
        	dispEULA.setMessage("By accepting this EULA, you agree to any terms that I feel like putting here.");
        	dispEULA.setTitle("End User License Agreement");
        	dispEULA.setPositiveButton("Accept!", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface arg0, int arg1) {
        			acceptedEULA();
        		}
        	});
        	dispEULA.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface arg0, int arg1) {
        			declinedEULA();
        		}
        	});
        	dispEULA.show();
    	}
    }
    
    private void acceptedEULA()
    {
    	//If the user accepts the EULA, this is saved in the preferences
    	prefs.edit().putBoolean("AcceptedEULA", true).commit();
    }
    
    private void declinedEULA()
    {
    	//If the user declines the EULA, the application closes
    	finish();
    }
    
    private void fillDocList()
    {
    	//This method sets up the adapter so the list of documents can be
    	//displayed
        List<DocumentMetadata> metas = service.getDocumentList();
        
        if(metas == null)
        {
        	AlertDialog.Builder failAlert = new AlertDialog.Builder(this);
        	failAlert.setMessage("I'm sorry, I couldn't access the document" +
        			" list. Try checking your internet connection.");
        	failAlert.setTitle("Oops!");
        	failAlert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface arg0, int arg1) {
        			
        		}
        	});
        	failAlert.show();
        }
        else
        {
        	currListInfo = new HashMap<Integer, String>();
        	ArrayList<String> titles = new ArrayList<String>();
	    	int i = 0;    	          
	        for (DocumentMetadata meta : metas) {
	        	currListInfo.put(i, meta.getKey());
	        	i++;
	        	titles.add(meta.getTitle());
	        }
	        
	    	currViewAdapter = new ArrayAdapter<String>(this, R.layout.row, 
	    			R.id.item_column, titles);
	    	setListAdapter(currViewAdapter);
        }
    }
    
    public void onClick(View v)
    {
    	//This method is called when the "New Document" button is clicked
    	//It starts the DocActivity activity, which will be populated with default fields
    	Intent i = new Intent(CollaboratorAndroidActivity.this, DocActivity.class);
    	startActivityForResult(i, DOC_ADD);
    }
    
    public void onListItemClick(ListView l, View v, int position, long id)
    {
    	/* THE FOLLOWING CODE ALLOWS USERS TO CHOOSE TO EDIT OR VIEW A DOC
    	 * DIRECTLY AFTER CLICKING ON THE ITEM IN THE LIST. I DECIDED TO
    	 * REMOVE IT, SINCE I FELT THAT IT WAS SLIGHTLY LESS INTUITIVE. */
    	//This method is called when a list item is selected
    	//An AlertDialog.Builder is displayed, with three buttons to choose
    	/*
    	AlertDialog.Builder itemAlert = new AlertDialog.Builder(this);
    	itemAlert.setMessage("Hello master, what would you like to do?");
    	itemAlert.setTitle(currViewAdapter.getItem(position));
    	currKey = currListInfo.get(position);
    	//If this button is chosen, the DocShow activity will be launched through the showDoc method
    	itemAlert.setPositiveButton("Show me!", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface arg0, int arg1) {
    			showDoc();
    		}
    	});
    	//If this button is chosen, the DocActivity activity will be launched, with its fields
    	//populated by the current information about the doc
    	itemAlert.setNeutralButton("Change me!", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface arg0, int arg1) {
    			editDoc();
    		}
    	});
    	itemAlert.show();
    	*/
    	currKey = currListInfo.get(position);
    	showDoc();
    }
    
    private void showDoc()
    {
    	//This method starts the DocShow activity for a user to see information
    	//entered about the specified doc
    	Intent i = new Intent(this, DocShow.class);
    	i.putExtra("docKey", currKey);
    	startActivityForResult(i, DOC_SHOW);
    }
    
    /* THIS METHOD IS ONLY USED IF THE USER HAS THE OPTION TO EDIT DIRECTLY
     * FROM THE DOCUMENT LIST. TO ENABLE THIS OPTION, UNCOMMENT THIS METHOD
     * AND FIX THE ONLISTITEMCLICK METHOD.
    private void editDoc()
    {
    	//This method starts the DocActivity activity, passing it
    	//the id of the list doc so the fields can be populated
    	//accordingly
    	Intent i = new Intent(this, DocActivity.class);
    	i.putExtra("docKey", currKey);
    	startActivityForResult(i, DOC_EDIT);
    }*/
    
    protected void onActivityResult(int reqCode, int resCode, Intent i)
    {
    	//When an activity finishes, this method is called
    	super.onActivityResult(reqCode, resCode, i);

    	if(i != null && i.hasExtra("toastText") && i.getStringExtra("toastText") != null)
        	Toast.makeText(this, i.getStringExtra("toastText"), Toast.LENGTH_LONG).show();

    	fillDocList(); //This refreshes the list being displayed
    }
}