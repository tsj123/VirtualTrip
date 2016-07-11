package com.eje_c.vrvideoplayer;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;

import com.eje_c.meganekko.Frame;
import com.eje_c.meganekko.Meganekko;
import com.eje_c.meganekko.MeganekkoApp;
import com.eje_c.meganekko.ObjectLookingStateDetector;
import com.eje_c.meganekko.SceneObject;

import android.util.Log;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;


public class VideoPlayerApp extends MeganekkoApp {

    //déclaration du tag
    public static final String TAG = "videoPlayerAppTag";

    private CountDownTimer waitTimer;
    private boolean tempo = false;
    private Timer timer = new Timer();
    private int counter = 0;
    private int idTimer = 0;

    private final MainActivity activity;
    private File file;
    private CanvasRenderer canvasRenderer;
    private MediaPlayer mediaPlayer;
    private SceneObject canvas;
    private SceneObject video;
    private Animator fadeInVideo, fadeOutCanvas;
    private ObjectLookingStateDetector detector;
    private boolean playing;

    private static boolean user = false; //"private" means access to this is restricted
    private static boolean pastUser = false; //"private" means access to this is restricted

    public static void getVRUser(boolean value) {
        Log.d(TAG, "getVRUser");
    }

    public static void setVRUser(boolean value) {
        Log.d(TAG, "setVRUser");
        user = value;
    }

    protected VideoPlayerApp(Meganekko meganekko, MainActivity activity) {
        super(meganekko);
        this.activity = activity;

        file = new File(Environment.getExternalStorageDirectory(), getContext().getString(R.string.video_path_from_sdcard));

        activity.showGazeCursor();
        setSceneFromXML(R.xml.scene);

        // get scene objects
        canvas = getScene().findObjectById(R.id.canvas);
        canvasRenderer = new CanvasRenderer(getContext());
        canvas.getRenderData().getMaterial().getTexture().set(canvasRenderer);

        video = getScene().findObjectById(R.id.video);

        // setup animations
        this.fadeInVideo = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
        fadeInVideo.setTarget(video);
        this.fadeOutCanvas = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_out);
        fadeOutCanvas.setTarget(canvas);

        //Initialiser le media player une bonne fois pour toutes
        if (mediaPlayer != null) {
            Log.d(TAG,"mediaPlayer pas null 01");
            release();
        }
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.video);
        startPlaying();

    }

    @Override
    public void update() {
        Log.d(TAG, "boucle update");

        if(!playing && user) {
            Log.d(TAG, "pas playing et user");
            playing = true;
            startPlaying();
        }

        else if(playing && !user) {
            Log.d(TAG, "playing et pas user");
            pause();
        }
        super.update();
    }

    @Override
    public void onPause() {
        super.onPause();
        runOnGlThread(new Runnable() {
            @Override
            public void run() {
                pause();
            }
        });
    }

    @Override
    public void onResume(){
        // on remet la vidéo au début
        Log.d(TAG,"video au debut");
        if (mediaPlayer != null) {
            Log.d(TAG,"mediaPlayer pas null 01");
            release();
        }
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.video);
    }

    @Override
    public void shutdown() {
        release();
        super.shutdown();
    }

    private void release() {
        Log.d(TAG,"release enclanche");
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
    }

    private void startPlaying() {

        Log.d(TAG,"start playing");

        playing = true;
        activity.hideGazeCursor();

/*On effectue ces lignes de code au début une seule fois
        if (mediaPlayer != null) {
            Log.d(TAG,"mediaPlayer pas null 01");
            release();
        }
        */
            //choisir entre la vidéo de la carte SD et la video par défaut
            //if (file.exists()) {
            //   Log.d(TAG,"file exist");
            //   mediaPlayer = MediaPlayer.create(getContext(), Uri.fromFile(file));
            //   Log.d(TAG,"mediaPlayer cree");
            //} else {
        //On effectue cette ligne de code au début une seule fois.
            //mediaPlayer = MediaPlayer.create(getContext(), R.raw.video);
            //  activity.getApp().showInfoText(3, getContext().getString(R.string.error_default_video));
            // }

            if (mediaPlayer != null) {
                Log.d(TAG, "mediaPlayer pas null 02");
                try {
                    Log.d(TAG, "mediaPlayer start");
                    mediaPlayer.start();
                    video.getRenderData().getMaterial().getTexture().set(mediaPlayer);

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            runOnGlThread(new Runnable() {
                                @Override
                                public void run() {
                                    pause();
                                }
                            });
                        }
                    });
                } catch (IllegalStateException e) {
                    activity.getApp().showInfoText(1, "error");
                    e.printStackTrace();
                }
            }

            if (canvas != null) {
                animate(fadeOutCanvas, new Runnable() {
                    @Override
                    public void run() {
                        canvas.setVisible(false);
                    }
                });
            }

            if (video != null) {
                animate(fadeInVideo, new Runnable() {
                    @Override
                    public void run() {
                        video.setVisible(true);
                    }
                });
            }
    }

    private void pause() {
        Log.d(TAG, "pause");
        playing = false;
        activity.showGazeCursor();

        if (mediaPlayer != null) {
            try {
                mediaPlayer.pause();
                //mediaPlayer.seekTo(0);
            } catch (IllegalStateException e) {
                activity.getApp().showInfoText(1, "error");
                e.printStackTrace();
            }
        }

        if (canvas != null) {
            canvas.setVisible(true);
            canvas.setOpacity(1.0f);
        }

        if (video != null) {
            video.setVisible(false);
        }
    }
}
