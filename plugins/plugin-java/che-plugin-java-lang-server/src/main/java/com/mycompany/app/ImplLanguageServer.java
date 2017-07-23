package com.mycompany.app;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class ImplLanguageServer implements LanguageServer {
	
	private TextDocumentService textService;
	private WorkspaceService workspaceService;
	LanguageClient client;
	
	public ImplLanguageServer() {
		textService = new ImplTextDocumentService(this);
		workspaceService = new ImplWorkspaceService();
		
	}
	

	public CompletableFuture<InitializeResult> initialize(InitializeParams params){
		// TODO Auto-generated method stub
		
		
			
		return null;
	}

	public CompletableFuture<Object> shutdown() {
		// TODO Auto-generated method stub
		return null;
	}

	public void exit() {
		// TODO Auto-generated method stub

	}

	public TextDocumentService getTextDocumentService() {
		// TODO Auto-generated method stub
		return null;
	}

	public WorkspaceService getWorkspaceService() {
		// TODO Auto-generated method stub
		return null;
	}

}

