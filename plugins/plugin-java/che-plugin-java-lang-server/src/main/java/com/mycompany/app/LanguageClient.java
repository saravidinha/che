package com.mycompany.app;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;

public class LanguageClient implements org.eclipse.lsp4j.services.LanguageClient {

	public void telemetryEvent(Object object) {
		// TODO Auto-generated method stub

	}

	public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
		// TODO Auto-generated method stub

	}

	public void showMessage(MessageParams messageParams) {
		// TODO Auto-generated method stub

	}

	public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
		// TODO Auto-generated method stub
		return null;
	}

	public void logMessage(MessageParams message) {
		// TODO Auto-generated method stub

	}

}
