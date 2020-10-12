package me.dillbox.mailmanmod;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.TaskStackBuilder;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.List;

import static android.text.Html.FROM_HTML_MODE_LEGACY;
import static android.text.Html.fromHtml;

public class RefreshWorker extends Worker {
    private static final String TAG = "RefreshWorker";

    public RefreshWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParameters) {
        super(appContext,workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {

        Context applicationContext = getApplicationContext();

        try {
            MailmanRepository repository = MailmanRepository.getInstance(applicationContext);
            String list = getInputData().getString("list");
            String list_display = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(list+"_display_name", list);
            String list_name = list_display.trim().isEmpty() ? list : list_display;

            String TAG = "worker-blyat";

            try {
                String url = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(list+"_url", "");
                String password = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(list+"_password", "");

                List<Integer> msgId_list = MailmanHTTPHelper.fetchMailQueueIds(url, password, applicationContext);
                List<Integer> oldMsgId_list = repository.findOldMsgId(list, msgId_list);
                for (Integer msgId : oldMsgId_list) {
                    repository.delete(list, msgId);
                }
                long latest = 0;
                List<String> newMail = new ArrayList<>();
                for (Integer msgId : msgId_list) {
                    HeldMail mail = repository.getById(list, msgId);
                    if (mail == null) {
                        mail = MailmanHTTPHelper.fetchMsgId(list, msgId, url, password, applicationContext);
                        repository.insert(mail);
                    }
                    String sender_display = mail.getName().trim();
                    if (sender_display.length() == 0) {
                        sender_display = mail.getEmail();
                    }
                    newMail.add(String.format("<b>%s</b> %s", sender_display, mail.getSubject()));
                    long timestamp = mail.getDateObject().getTime();
                    if (timestamp > latest) {
                        latest = timestamp;
                    }
                }
                if (!newMail.isEmpty()) {
                    String CHANNEL_ID = list + "-notification";
                    Intent intent = new Intent(applicationContext, MainActivity.class);
                    intent.putExtra("list", list);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(applicationContext);
                    stackBuilder.addNextIntentWithParentStack(intent);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                    for (String mail : newMail) {
                        inboxStyle.addLine(fromHtml(mail));
                    }
                    int count = newMail.size();
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_mailman_envelope)
                            .setStyle(inboxStyle)
                            .setContentTitle(String.format("[%s] ", list_display)+applicationContext.getResources().getQuantityString(R.plurals.new_messages, count, count))
                            .setContentIntent(pendingIntent)
                            .setWhen(latest)
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_HIGH);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
                    notificationManager.notify(list.hashCode(), builder.build());
                }
                /*for (Integer msgId : msgId_list) {
                    if (!repository.hasMsgId(list, msgId)) {
                        HeldMail mail = MailmanHTTPHelper.fetchMsgId(list, msgId, url, password, applicationContext);
                        repository.insert(mail);
                        String CHANNEL_ID = list + "-notification";
                        Intent intent = new Intent(applicationContext, MainActivity.class);
                        intent.putExtra("list", list);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(applicationContext);
                        stackBuilder.addNextIntentWithParentStack(intent);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                                .setSmallIcon(android.R.drawable.ic_dialog_email)
                                .setContentTitle(String.format(applicationContext.getString(R.string.notification_title), list_name, mail.getName()))
                                .setContentText(mail.getSubject())
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_HIGH);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
                        notificationManager.notify(list.hashCode()+msgId, builder.build());
                    }
                }*/
            } catch (MailmanHTTPHelper.MailmanHTTPException e) {
                repository.deleteAll(list);
                Log.e(TAG, "run: ", e);
                throw e;
            }
            return Result.success();
        } catch (Throwable throwable) {

            // Technically WorkManager will return Result.failure()
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE
            Log.e(TAG, "Error in worker", throwable);
            return Result.failure();
        }
    }
}
