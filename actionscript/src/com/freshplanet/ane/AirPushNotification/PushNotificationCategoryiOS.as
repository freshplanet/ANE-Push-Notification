package com.freshplanet.ane.AirPushNotification {
public class PushNotificationCategoryiOS {

	public var id:String;

	public var hiddenSummaryKey:String;
	public var hiddenSummaryKeyFile:String;

	public var summaryKey:String;
	public var summaryKeyFile:String;

	public var action:PushNotificationCategoryActioniOS;

	public function PushNotificationCategoryiOS(
			id:String,
			hiddenSummaryKey:String,
			hiddenSummaryKeyFile:String,
			summaryKey:String,
			summaryKeyFile:String,
			action:PushNotificationCategoryActioniOS
	) {
		this.id = id;
		this.hiddenSummaryKey = hiddenSummaryKey;
		this.hiddenSummaryKeyFile = hiddenSummaryKeyFile;
		this.summaryKey = summaryKey;
		this.summaryKeyFile = summaryKeyFile;
		this.action = action;
	}

}
}
