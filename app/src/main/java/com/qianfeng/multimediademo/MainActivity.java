package com.qianfeng.multimediademo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 多媒体处理相关类，包括调用系统播放器播放音乐，播放视频，调用系统录音机录音，调用相机录像，拍照
 */
public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void choicePic(View view)
    {
        //1.选择图片
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"
        intent.setType("image/*"); //查看类型 String IMAGE_UNSPECIFIED = "image/*";
        Intent wrapperIntent = Intent.createChooser(intent, null);
        startActivityForResult(wrapperIntent, 1111);

//        playNetVideo();
    }

    public void playAudio(View view)
    {
        //2.播放音频
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*"); //String VIDEO_UNSPECIFIED = "video/*";
        Intent wrapperIntent = Intent.createChooser(intent, null);
        startActivityForResult(wrapperIntent, 2222);
    }

    public void takeVideo(View view)
    {
        //3.拍摄视频
//        int durationLimit = getVideoCaptureDurationLimit(); //SystemProperties.getInt("ro.media.enc.lprof.duration", 60);
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        int sizeLimit = 1024;
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, sizeLimit);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
        startActivityForResult(intent, 3333);
    }

    public void playVideo(View view)
    {
        //4.播放视频
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*"); //String VIDEO_UNSPECIFIED = "video/*";
        Intent wrapperIntent = Intent.createChooser(intent, "选择要播放的视频");
        startActivityForResult(wrapperIntent, 4444);

    }

    public void recordAudio(View view)
    {
        //5.录音
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/amr"); //String AUDIO_AMR = "audio/amr";
        intent.setClassName("com.android.soundrecorder", "com.android.soundrecorder.SoundRecorder");
        startActivityForResult(intent, 5555);
    }

    public void takePhoto(View view)
    {
        //6.拍照 REQUEST_CODE_TAKE_PICTURE 为返回的标识
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //"android.media.action.IMAGE_CAPTURE";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            //文件夹aaaa
            String path = Environment.getExternalStorageDirectory().toString() + "/aaaa";
            File path1 = new File(path);
            if (!path1.exists())
            {
                boolean mkdirs = path1.mkdirs();
            }
            File file = new File(path1, System.currentTimeMillis() + ".jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file)); // output,Uri.parse("content://mms/scrapSpace");
        }
        startActivityForResult(intent, 6666);
    }

    /**
     * 使用getData返回一个Uri,再使用context.getContentResolver().openInputStream(uri);可以拿到一个输入流
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        /**
         * 如果不注释掉此句，在发送系统Intent后不执行选择操作，直接返回会报空指针
         */
//        super.onActivityResult(requestCode, resultCode, data);
        if (data != null)
        {
            Uri dataUri = data.getData();
            Intent intent;
            switch (requestCode)
            {
                case 1111:
                    try
                    {
                        InputStream in = getContentResolver().openInputStream(dataUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(in);
                        ImageView imageView = (ImageView) findViewById(R.id.load_image);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageBitmap(bitmap);

                    } catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    break;
                case 2222:
                    intent = new Intent(Intent.ACTION_VIEW);

                    //Uri---->绝对路径
                    String path = GetPathFromUri4kitkat.getPath(this, dataUri);
                    if (path == null)
                    {
                        return;
                    }
                    File file = new File(path);
                    Log.e("tag", "原始Uri:" + dataUri.toString());
                    dataUri = Uri.fromFile(file);
                    Log.e("tag", "转换后的Uri:" + dataUri.toString());

                    //文件路径---->扩展名
                    String fileExtension = path.substring(path.lastIndexOf(".") + 1, path.length());
                    Log.e("tag", "fileExtension:" + fileExtension);

                    //文件扩展名---->MimeType
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                    Log.e("tag", "mimeType:" + mimeType);

                    intent.setDataAndType(dataUri, mimeType);
                    startActivity(intent);

                    break;
                case 3333:

                    break;

                case 4444:
                    //播放本地视频，系统播放器
                    playLocalVideoBySystemVideoPlayer(dataUri);
                    //播放本地视频，VideoView
//                    playLocalVideoByVideoView(dataUri);
                    //播放网络视频，系统播放器
//                    playNetworkVideoBySystemVideoPlayer();

                    break;
                case 5555:

                    break;
                case 6666:

                    break;
            }
        }
//        Log.e("tag", "requestCode:" + requestCode);
//        Log.e("tag", "resultCode:" + resultCode);
//        Log.e("tag", "data:" + data);
//        Log.e("tag", "----------------------------------------------");
    }

    /**
     * 调用系统视频播放器，播放本地视频文件
     *
     * @param dataUri Intent.createChooser(intent, "选择要播放的视频")得到的Uri
     */
    private void playLocalVideoBySystemVideoPlayer(Uri dataUri)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        //Uri---->绝对路径
        String path = GetPathFromUri4kitkat.getPath(this, dataUri);
        if (path == null)
        {
            return;
        }
        File file = new File(path);
        Log.e("tag", "原始Uri:" + dataUri.toString());
        dataUri = Uri.fromFile(file);
        Log.e("tag", "转换后的Uri:" + dataUri.toString());

        //文件路径---->扩展名
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(path);
        //文件扩展名---->MimeType
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);

        intent.setDataAndType(dataUri, mimeType);
        startActivity(intent);
    }

    /**
     * 调用系统视频播放器，播放网络视频文件
     */
    private void playNetworkVideoBySystemVideoPlayer()
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String videoUrl = "http://218.200.69.66:8302/upload/Media/20150327/43bfda1b-7280-469c-a83b-82fa311c79d7.m4v";
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(videoUrl);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        Uri parse = Uri.parse(videoUrl);
        intent.setDataAndType(parse, mimeType);
        startActivity(intent);
    }

    /**
     * 通过VideoView，播放本地视频文件
     *
     * @param dataUri Intent.createChooser(intent, "选择要播放的视频")得到的Uri
     */
    private void playLocalVideoByVideoView(Uri dataUri)
    {
        VideoView videoView = (VideoView) findViewById(R.id.act_video_play);
        videoView.setVisibility(View.VISIBLE);
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(dataUri);
        videoView.start();
        videoView.requestFocus();
    }
}
