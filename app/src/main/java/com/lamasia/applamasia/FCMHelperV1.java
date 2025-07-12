package com.lamasia.applamasia;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.util.Scanner;

public class FCMHelperV1 {

    private static final String TAG = "FCMHelperV1";
    private static final String SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String FCM_ENDPOINT = "https://fcm.googleapis.com/v1/projects/%s/messages:send";
    private static final String SERVICE_ACCOUNT_FILE = "firebase-key.json"; // ✅ archivo en assets

    public static void enviarNotificacionATopic(Context context, String topic, String titulo, String mensaje) {
        try {
            // ⚠️ Permitir red en hilo principal temporalmente
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // Cargar credenciales desde assets
            InputStream serviceAccount = context.getAssets().open(SERVICE_ACCOUNT_FILE);
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                    .createScoped(Collections.singleton(SCOPE));
            credentials.refreshIfExpired();

            String accessToken = credentials.getAccessToken().getTokenValue();
            String projectId = ((com.google.auth.oauth2.ServiceAccountCredentials) credentials).getProjectId();

            // Crear estructura del mensaje
            JSONObject json = new JSONObject();
            JSONObject message = new JSONObject();
            message.put("topic", topic);

            // Notificación visible
            JSONObject notification = new JSONObject();
            notification.put("title", titulo);
            notification.put("body", mensaje);
            message.put("notification", notification);

            // Opciones Android con canal y sonido
            JSONObject android = new JSONObject();
            JSONObject notificationAndroid = new JSONObject();
            notificationAndroid.put("sound", "default");
            notificationAndroid.put("channel_id", "default_channel_id");
            android.put("notification", notificationAndroid);
            message.put("android", android);

            json.put("message", message);

            // Enviar solicitud HTTPS
            URL url = new URL(String.format(FCM_ENDPOINT, projectId));
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json; UTF-8");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes("UTF-8"));
            os.close();

            int responseCode = conn.getResponseCode();
            Scanner scanner = new Scanner(
                    responseCode == HttpURLConnection.HTTP_OK ? conn.getInputStream() : conn.getErrorStream()
            );
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNext()) sb.append(scanner.nextLine());
            scanner.close();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "✅ Notificación enviada: " + sb.toString());
            } else {
                Log.e(TAG, "❌ Error al enviar notificación: " + sb.toString());
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Excepción al enviar notificación: ", e);
        }
    }
}
