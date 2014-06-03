//////////////////////////////////////////////////////////////////////////////////////
//
//  Copyright 2012 Freshplanet (http://freshplanet.com | opensource@freshplanet.com)
//  
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//  
//    http://www.apache.org/licenses/LICENSE-2.0
//  
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  
//////////////////////////////////////////////////////////////////////////////////////

package com.freshplanet.nativeExtensions;

import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREInvalidObjectException;
import com.adobe.fre.FREObject;
import com.adobe.fre.FRETypeMismatchException;
import com.adobe.fre.FREWrongThreadException;

public class SetIsAppInForegroundFunction implements FREFunction {

	public FREObject call(FREContext arg0, FREObject[] arg1) {

		boolean isInForeground;
		try {
			isInForeground = arg1[0].getAsBool();
			if(isInForeground)
			{
				MultiMsgNotification msg = MultiMsgNotification.Instance(arg0.getActivity());
				msg.remove();
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return null;
		} catch (FRETypeMismatchException e) {
			e.printStackTrace();
			return null;
		} catch (FREInvalidObjectException e) {
			e.printStackTrace();
			return null;
		} catch (FREWrongThreadException e) {
			e.printStackTrace();
			return null;
		}
		
		C2DMExtension.isInForeground = isInForeground;

		return null;
	}

}
