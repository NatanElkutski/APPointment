package com.example.appointment.Service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.ContentLoadingProgressBar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adamstyrc.cookiecutter.CookieCutterImageView;
import com.bumptech.glide.Glide;
import com.example.appointment.Common.Common;
import com.example.appointment.Model.profileImage;
import com.example.appointment.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Button mButtonChooseImage;
    private Button mButtonUpload,mButtonCamera;
    private TextView mTextViewShowUploads;
    private EditText mEditTextFileName;
    private RoundedImageView imageView;
    private ContentLoadingProgressBar mProgressBar;
    AlertDialog dialog;
    private Uri mImageUri;
    private StorageReference mStorageRef;
    private CollectionReference UploadRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        dialog = new SpotsDialog.Builder().setContext(UploadActivity.this).setCancelable(false).build();
        mButtonChooseImage = findViewById(R.id.choose_file);
        mButtonUpload = findViewById(R.id.upload_image);
        imageView = findViewById(R.id.upload_image_view);
        mStorageRef = FirebaseStorage.getInstance().getReference("Uploads");
        UploadRef = FirebaseFirestore.getInstance().collection("User").document(Common.fuser.getUid()).collection("Uploads");


        if(Common.profilePicture!="") {
            Glide.with(UploadActivity.this)
                    .load(Common.profilePicture)
                    .circleCrop()
                    .into(imageView);
        }


        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                uploadFile();

            }
        });
    }

    private void openFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            imageView.setOutlineAmbientShadowColor(Color.BLACK);
            Glide.with(UploadActivity.this)
                    .load(mImageUri)
                    .circleCrop()
                    .into(imageView);
        }
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if(mImageUri != null)
        {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()+"."+getFileExtension(mImageUri));

                fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            dialog.dismiss();
                            Toast.makeText(UploadActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                            profileImage upload = new profileImage (Common.currentUser.getEmail().toString().trim(),url);
                            UploadRef.document("profileImage").set(upload);
                            Common.profilePicture = upload.getImageUrl();
                            startActivity(new Intent(UploadActivity.this,MainActivity.class));
                            finish();
                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            dialog.dismiss();
            Toast.makeText(this, "No file Selected", Toast.LENGTH_SHORT).show();
        }
    }

}