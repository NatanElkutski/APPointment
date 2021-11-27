package com.example.appointment.SendNotificationPack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAALFDE_P0:APA91bEeRN8HeJm0tJ3gelWvUvGcDxSsQMD_2DZp3oHP5Ymv8IMgKgqKxIZXUmUWr2GF-s0TfYeHjCgpr2jHWQ6Hp_f85HTQ8B112PwQrcvAtTtxfLpEEr4sHzRsmqm1gmAN-SSIkOsL"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification (@Body NotificationSender body);
}
