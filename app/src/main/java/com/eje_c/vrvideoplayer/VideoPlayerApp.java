package com.eje_c.vrvideoplayer;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.eje_c.meganekko.Frame;
import com.eje_c.meganekko.Meganekko;
import com.eje_c.meganekko.MeganekkoApp;
import com.eje_c.meganekko.ObjectLookingStateDetector;
import com.eje_c.meganekko.SceneObject;

import android.util.Log;

import java.io.File;


public class VideoPlayerApp extends MeganekkoApp {

    //déclaration du tag
    public static final String TAG = "videoPlayerAppTag";

    private final MainActivity activity;
    private File file;
    private CanvasRenderer canvasRenderer;
    private MediaPlayer mediaPlayer;
    private SceneObject canvas;
    private SceneObject video;
    private Animator fadeInVideo, fadeOutCanvas;
    private ObjectLookingStateDetector detector;
    private boolean playing;

    private static boolean user = true; //"private" means access to this is restricted

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

        /*
        // animation while looking at start button
        detector = new ObjectLookingStateDetector(this, canvas, new ObjectLookingStateDetector.ObjectLookingStateListener() {
            boolean notified;

            @Override
            public void onLookStart(SceneObject sceneObject, Frame frame) {
                canvasRenderer.setLooking(true);
            }

            @Override
            public void onLooking(SceneObject sceneObject, Frame frame) {
                canvasRenderer.update(frame);

                if (!notified && canvasRenderer.getSweepFraction() >= 1.0f) {
                    notified = true;
                    startPlaying();
                }
            }

            @Override
            public void onLookEnd(SceneObject sceneObject, Frame frame) {
                canvasRenderer.setLooking(false);
            }
        });
        */
    }

    @Override
    public void update() {

        //lancer la vidéo en bouclant
        if(!playing && user) {
            startPlaying();
        }

        // NOT WORKING
        else if(playing && !user){
            pause();
        }

        /* Cette condition  faisait quitter l'application lors de la fin de la vidéo en l'abscence d'utilisateur
        if (!playing) {
            detector.update(getFrame());
        }
        */
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
    public void shutdown() {
        release();
        super.shutdown();
    }

    private void release() {
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


        if (mediaPlayer != null) {
            Log.d(TAG,"mediaPlayer pas null 01");
            release();
        }

        //choisir entre la vidéo de la carte SD et la video par défaut
        //if (file.exists()) {
         //   Log.d(TAG,"file exist");
         //   mediaPlayer = MediaPlayer.create(getContext(), Uri.fromFile(file));
         //   Log.d(TAG,"mediaPlayer cree");
        //} else {
            mediaPlayer = MediaPlayer.create(getContext(), R.raw.video);
          //  activity.getApp().showInfoText(3, getContext().getString(R.string.error_default_video));
       // }

        if (mediaPlayer != null) {
            Log.d(TAG,"mediaPlayer pas null 02");
            try {
                Log.d(TAG,"mediaPlayer start");
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
        playing = false;
        activity.showGazeCursor();

        if (mediaPlayer != null) {
            try {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
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
