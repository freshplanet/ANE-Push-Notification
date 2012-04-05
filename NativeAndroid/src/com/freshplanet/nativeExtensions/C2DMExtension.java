package com.freshplanet.nativeExtensions;

import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREExtension;

public class C2DMExtension implements FREExtension {

	public static FREContext context;
	
	/**
	 * Create the context (AS to Java).
	 */
	public FREContext createContext(String extId) {
		Log.d("as3c2dm", "C2DMExtension.createContext extId: " + extId);
		return context = new C2DMExtensionContext();
	}

	/**
	 * Dispose the context.
	 */
	public void dispose() {
		Log.d("as3c2dm", "C2DMExtension.dispose");
		context = null;
	}
	
	/**
	 * Initialize the context.
	 * Doesn't do anything for now.
	 */
	public void initialize() {
		Log.d("as3c2dm", "C2DMExtension.initialize");
	}
}
