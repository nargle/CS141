package edu.caltech.cs141b.hw5.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.caltech.cs141b.hw5.data.CollabMessages.RequestMessage;
import edu.caltech.cs141b.hw5.data.CollabMessages.ResponseMessage;
import edu.caltech.cs141b.hw5.data.CollabMessages.StatusType;
import edu.caltech.cs141b.hw5.gwt.collab.server.CollaboratorServiceImpl;
import edu.caltech.cs141b.hw5.gwt.collab.shared.DataConverter;
import edu.caltech.cs141b.hw5.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw5.gwt.collab.shared.UnlockedDocument;

/**
 * HttpServlet maps HTTP requests to function calls to corresponding methods in 
 * CollaboratorServiceImpl and wraps the response to HTTP responses. It uses 
 * Protocol Buffer for serializing and deserializing the objects. 
 * 
 * @author aliu
 *
 */
@SuppressWarnings("serial")
public class CollaboratorMessageServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(CollaboratorMessageServlet.class.getName());
	
	private CollaboratorServiceImpl collabService = new CollaboratorServiceImpl();
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) {
		
		log.info("Received request");
		try {
			RequestMessage request;

			try {
				// Deserializing
				request = RequestMessage.parseFrom(req.getInputStream());
				log.info("successfully parsed request");
			} catch (IOException e) {
				log.severe("IOException thrown when getting input strea: " + e.getMessage());
				return;
			}		
			writeBuffer(resp, onMessage(request, req.getRemoteAddr()));
		} catch (IOException e) {
			log.severe("IOException thrown when getting output stream in doPost: " + e.getMessage());
		} finally {			
		}
	}
	
	private static void writeBuffer(HttpServletResponse resp, 
			ResponseMessage result) throws IOException {
		resp.setContentType("application/x-protobuf");
		resp.setContentLength(result.getSerializedSize());
		result.writeTo(resp.getOutputStream());
	}

	/**
	 * Maps the request actions to corresponding function calls to CollaboratorServiceImpl
	 * based on the RequestType. Wraps the returned objects into a ResponseMessage.
	 * 
	 * @param request
	 * @param ip
	 * @return
	 */
	private ResponseMessage onMessage(RequestMessage request, String ip) {
		
		log.info("starting processing request");
		ResponseMessage.Builder builder = ResponseMessage.newBuilder();
		
		if (!request.hasRequestType()) {
			builder.setStatusType(StatusType.INVALID_REQUEST);
			builder.setMessage("No RequestType set in RequestMessage.");
		} else {
			switch (request.getRequestType()) {	
				case GET_DOCUMENT_LIST:
					List<DocumentMetadata> docList = collabService.getDocumentList();
					for (DocumentMetadata meta : docList) {
						builder.addDocMeta(DataConverter.buildDocumentMetaInfo(meta));
					}
					builder.setStatusType(StatusType.SUCCESS);
					break;
				case LOCK_DOCUMENT:
					if (request.hasDocumentKey()) {
						try {
							LockedDocument doc = collabService.lockDocument(
									request.getDocumentKey(), ip);
							builder.setStatusType(StatusType.SUCCESS);
							builder.setLockedDoc(DataConverter.buildLockedDocumentInfo(doc));
						} catch (LockUnavailable e) {
							builder.setStatusType(StatusType.LOCK_UNAVAILABLE);
							builder.setMessage(e.getMessage());
						}
					} else {
						builder.setStatusType(StatusType.INVALID_REQUEST);
						builder.setMessage("No document key in RequestMessage.");
					}
					break;
				case GET_DOCUMENT:
					if (request.hasDocumentKey()) {
						UnlockedDocument doc = collabService.getDocument(request.getDocumentKey());
						builder.setUnlockedDoc(DataConverter.buildUnlockedDocumentInfo(doc));
						builder.setStatusType(StatusType.SUCCESS);
					} else {
						builder.setStatusType(StatusType.INVALID_REQUEST);
						builder.setMessage("No document key in RequestMessage.");
					}
					break;
				case SAVE_DOCUMENT:
					if (request.hasLockedDoc()) {
						try {
							UnlockedDocument doc = collabService.saveDocument(
									DataConverter.buildLockedDocument(request.getLockedDoc()), ip);
							builder.setUnlockedDoc(DataConverter.buildUnlockedDocumentInfo(doc));
							builder.setStatusType(StatusType.SUCCESS);
						} catch (LockExpired e) {
							builder.setStatusType(StatusType.LOCK_EXPIRED);
							builder.setMessage(e.getMessage());
						}
					} else {
						builder.setStatusType(StatusType.INVALID_REQUEST);
						builder.setMessage("No locked document in RequestMessage.");
					}
					break;
				case RELEASE_LOCK:
					if (request.hasLockedDoc()) {
						try {
							collabService.releaseLock(DataConverter.buildLockedDocument(
									request.getLockedDoc()), ip);
							builder.setStatusType(StatusType.SUCCESS);
						} catch (LockExpired e) {
							builder.setStatusType(StatusType.LOCK_EXPIRED);
						}
					} else {
						builder.setStatusType(StatusType.INVALID_REQUEST);
						builder.setMessage("No locked document in RequestMessage.");
					}
					break;		
			}
		}
		log.info("finished processing request");
		return builder.build();
	}
}
