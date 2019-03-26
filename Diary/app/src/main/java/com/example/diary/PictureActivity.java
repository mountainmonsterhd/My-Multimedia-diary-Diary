package com.example.diary;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class PictureActivity extends AppCompatActivity {
    private boolean taken_photo;
    private boolean opened_photo;

    Bitmap bitmap;
    private Uri imageUri;
    private String filePath;
    private DialogInterface.OnClickListener confirm;
    private DialogInterface.OnClickListener cancel;


    private void initial_dialog(){
        confirm=new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0,int arg1)
            {
                Intent i = new Intent();
                i.putExtra("changed", false);
                PictureActivity.this.setResult(Protocl.ADD_PICTURE, i);
                if(taken_photo){
                    File delete_file = new File(filePath);
                    delete_file.delete();
                    String[] path = {filePath};
                    PictureActivity.this.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?",path);
                }
                finish();
            }
        };
        cancel=new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0,int arg1)
            {
                arg0.cancel();
            }
        };
    }


    //Create the activity
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(PictureActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, Protocl.STORAGE_PERMISSION);
        ActivityCompat.requestPermissions(PictureActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Protocl.READE_PERMISSION);

        setContentView(R.layout.picture);
        taken_photo = false;
        opened_photo = false;
        initial_dialog();
    }

    //back to last activity
    public void back_picture(View view) {
        AlertDialog.Builder alert_dialog_builder = new AlertDialog.Builder(this);
        alert_dialog_builder.setMessage("Leave without saving?");
        alert_dialog_builder.setPositiveButton("confirm", confirm);
        alert_dialog_builder.setNegativeButton("cancel", cancel);
        AlertDialog alert_dialog = alert_dialog_builder.create();
        alert_dialog.show();
    }

    public void copy_Bitmap(String from_File){
        String save_File_Name = Environment.getExternalStorageDirectory() + "/Diary_Data/Images/" + System.currentTimeMillis();
        File save_File = new File(save_File_Name);
        if (!save_File.getParentFile().exists()) {
            save_File.getParentFile().mkdirs();
        }
        else if(save_File.exists()||save_File.isDirectory()){
            save_File.delete();
        }
        try {
            InputStream inputStream = new FileInputStream(from_File);
            FileOutputStream outputStream = new FileOutputStream(save_File);
            byte[] bytes = new byte[1024];
            int i;
            while ((i = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, i);
            }
            inputStream.close();
            outputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        filePath = save_File_Name;
    }
    //complete picture select and add into the diary
    public void complete_picture(View view) {
        //to do:add your media
        if(opened_photo||taken_photo) {
            Intent i = new Intent();
            if (opened_photo) {
                copy_Bitmap(filePath);
            }
            i.putExtra("image_path", filePath);
            i.putExtra("changed", true);
            this.setResult(Protocl.ADD_PICTURE, i);
            finish();
        }
        else{
            Toast.makeText(this, "You did not choose anything!", Toast.LENGTH_SHORT).show();
        }
    }

    // key down
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder alert_dialog_builder = new AlertDialog.Builder(this);
            alert_dialog_builder.setMessage("Leave without saving?");
            alert_dialog_builder.setPositiveButton("confirm", confirm);
            alert_dialog_builder.setNegativeButton("cancel", cancel);
            AlertDialog alert_dialog = alert_dialog_builder.create();
            alert_dialog.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    //take photos
    public void take_Photo(View view) {
        if (ContextCompat.checkSelfPermission(PictureActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PictureActivity.this, new String[]{Manifest.permission.CAMERA}, Protocl.CAMERA_PERMISSION);
        }
        else{
            openCamera();
        }
        // 启动相机程序

    }

    private void openCamera(){
        filePath = Environment.getExternalStorageDirectory() + "/Diary_Data/Images/" + System.currentTimeMillis() + ".jpg";
        File outputImage = new File(filePath);
        if (!outputImage.getParentFile().exists()) {
            outputImage.getParentFile().mkdirs();
        }
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //判断版本号
        if (Build.VERSION.SDK_INT < 24) {
            imageUri = Uri.fromFile(outputImage);
        } else {
            imageUri = FileProvider.getUriForFile(PictureActivity.this, "com.example.diary.my_provider", outputImage);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, Protocl.TAKE_PHOTO);
    }

    //open file
    public void open_picture_file(View view) {
        //如果没有权限则申请权限
        if (ContextCompat.checkSelfPermission(PictureActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PictureActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Protocl.OPEN_PERMISSION);
        }
       else{
           openAlbum();
        }
    }

    //open Album
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, Protocl.CHOOSE_PHOTO); // 打开相册
    }

    //permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Protocl.CAMERA_PERMISSION:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
            }break;
            case Protocl.OPEN_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    //RETURN THE PICTURE
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case Protocl.TAKE_PHOTO: {
                try {// 将拍摄的照片显示出来
                    File file = new File(filePath);
                    if(file.exists()) {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        int degree = readPictureDegree(file.getAbsolutePath());
                        bitmap = rotate_ImageView(degree, bitmap);
                        ((ImageView) findViewById(R.id.Picture_content)).setImageBitmap(bitmap);
                        taken_photo = true;
                        opened_photo = false;
                    }
                    else{
                        Toast.makeText(this, "You canceled take photo!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case Protocl.CHOOSE_PHOTO: {
                //choose your photo
                // 判断手机系统版本号
                if (Build.VERSION.SDK_INT >= 19) {
                    // 4.4及以上系统使用这个方法处理图片
                    handleImageOnKitKat(data);
                } else {
                    // 4.4以下系统使用这个方法处理图片
                    handleImageBeforeKitKat(data);
                }
            }
            break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        if(data != null) {
            Uri uri = data.getData();
            Log.d("TAG", "handleImageOnKitKat: uri is " + uri);

            if (DocumentsContract.isDocumentUri(this, uri)) {
                // 如果是document类型的Uri，则通过document id处理
                String docId = DocumentsContract.getDocumentId(uri);
                if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String id = docId.split(":")[1]; // 解析出数字格式的id
                    String selection = MediaStore.Images.Media._ID + "=" + id;
                    imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    imagePath = getImagePath(contentUri, null);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                // 如果是content类型的Uri，则使用普通方式处理
                imagePath = getImagePath(uri, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                // 如果是file类型的Uri，直接获取图片路径即可
                imagePath = uri.getPath();
            }
            displayImage(imagePath); // 根据图片路径显示图片
        }
        else{
            opened_photo = false;
            Toast.makeText(this, "You canceled opening", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String Path) {
        if (Path != null) {
            filePath = Path;
            bitmap = BitmapFactory.decodeFile(Path);
            int degree = getDegree(Path);
            bitmap = rotate_ImageView(degree, bitmap);
            ((ImageView) findViewById(R.id.Picture_content)).setImageBitmap(bitmap);
            opened_photo = true;
            taken_photo = false;
        } else {
            Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
        }
    }
    //获取拍摄的方向
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }
    //获取图片的方向
    public static int getDegree(String path) {
        int digree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            exif = null;
        }
        if (exif != null) {
            // 读取图片中相机方向信息
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            // 计算旋转角度
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    digree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    digree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    digree = 270;
                    break;
                default:
                    digree = 0;
                    break;
            }
        }
        return digree;
    }
//旋转图片
    public static Bitmap rotate_ImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        ;
        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }
}
