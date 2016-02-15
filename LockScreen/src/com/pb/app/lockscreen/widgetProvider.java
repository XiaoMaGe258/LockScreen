package com.pb.app.lockscreen;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class widgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		Intent in = new Intent(context, LockScreenActivity.class);
//		Intent in = new Intent("PREV");
		PendingIntent pendingIntent = PendingIntent.getActivity(context,
				0, in, 0);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.lockwidget);
		remoteViews.setOnClickPendingIntent(R.id.image_widget, pendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
		
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

}
