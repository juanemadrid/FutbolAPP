package com.lamasia.applamasia.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lamasia.applamasia.R;
import com.lamasia.applamasia.AvisosActivity;

import java.util.Collections;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CANAL_ID = "default_channel_id";

    @Override
    public void onCreate() {
        super.onCreate();
        crearCanalNotificaciones();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            String correo = FirebaseAuth.getInstance().getCurrentUser() != null ?
                    FirebaseAuth.getInstance().getCurrentUser().getEmail() : "";

            // Solo mostrar notificación si no es el administrador
            if (correo != null && !correo.equalsIgnoreCase("admin@lamasia.com")) {
                mostrarNotificacion(
                        remoteMessage.getNotification().getTitle(),
                        remoteMessage.getNotification().getBody()
                );
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        FirebaseFirestore.getInstance()
                .collection("user_tokens")
                .document(token)
                .set(Collections.singletonMap("token", token));
    }

    private void mostrarNotificacion(String titulo, String mensaje) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, AvisosActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CANAL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setAutoCancel(true)
                .setSound(sonido)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        manager.notify((int) System.currentTimeMillis(), builder.build()); // ID único por notificación
    }

    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager.getNotificationChannel(CANAL_ID) == null) {
                Uri sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationChannel canal = new NotificationChannel(
                        CANAL_ID,
                        "Notificaciones AppLaMasia",
                        NotificationManager.IMPORTANCE_HIGH
                );
                canal.setDescription("Canal para notificaciones generales");
                canal.enableLights(true);
                canal.enableVibration(true);
                canal.setSound(sonido, new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
                manager.createNotificationChannel(canal);
            }
        }
    }
}
