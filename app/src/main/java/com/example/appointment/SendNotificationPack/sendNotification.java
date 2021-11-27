package com.example.appointment.SendNotificationPack;

import android.app.Application;
import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class sendNotification extends Application {


    private static APIService apiService;


    public static void sendNotifications(String userToken, String title, String message) {
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, userToken);
        apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Log.d("Notification Status :", "Notification Failed!");
                    } else Log.d("Notification Status :", "Notification Sent!");
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
                Log.d("Notification Status :", t.getMessage());
            }
        });
    }
}
