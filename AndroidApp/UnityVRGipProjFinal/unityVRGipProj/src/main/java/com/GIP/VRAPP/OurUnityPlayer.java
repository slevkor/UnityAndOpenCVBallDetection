package com.GIP.VRAPP;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * Created by lev on 07/09/2016.
 */
public class OurUnityPlayer  extends UnityPlayerActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "OurDebugTag";
    private static int camId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private CameraBridgeViewBase mOpenCvCameraView;
    Mat mRgba;
    static public int r,g,b;
    static public double x,y,Dis;
    private ColorBlobDetector mDetector;
    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    private Size SPECTRUM_SIZE;
    private boolean isSecondSceneFirstTime = false;

    public static SceneEnum OurScene;
    public enum SceneEnum{
        First,
        Second
    }

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
        }
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.v(TAG,"OnCreate");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        JavaCameraView myView = new JavaCameraView(getApplicationContext(),camId);
        addContentView(myView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        mOpenCvCameraView = myView;
        mOpenCvCameraView.enableView();
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        OurScene = SceneEnum.First;
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        SPECTRUM_SIZE = new Size(200,64);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        if (OurScene == SceneEnum.First){
            int height = mRgba.height();
            int width = mRgba.width();
            double[] pixel = mRgba.get(((int)height/2),((int)width/2));
            r = (int)pixel[0];
            g = (int)pixel[1];
            b = (int)pixel[2];
            Log.v(TAG, "Im in Scene1 " + " R: " + Integer.toString(r) + " G: " + Integer.toString(g) + " B: " + Integer.toString(b));
        }
        else if (OurScene == SceneEnum.Second) {
            if (!isSecondSceneFirstTime){
                ChooseColorFromMiddle();
                isSecondSceneFirstTime=true;
            }
            Log.e(TAG, "Im in Scene2 ");
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());

            for (int i = 0; i < contours.size(); i++) {
                float[] radius = new float[1];
                Point center = new Point();
                Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(i).toArray()), center, radius);
                x = center.x/mRgba.cols();
                y = 1-(center.y/mRgba.rows());
                // distance will be from 1 to 15
                double diameter = (radius[0]*2);
                if (radius[0]*2>mRgba.rows()){
                    diameter = mRgba.rows();
                }
                //Dis = 15 - (diameter/mRgba.rows())*14;
                Dis = 1/(diameter/mRgba.rows());
                //Imgproc.drawMarker(mRgba, new Point(x,y),new Scalar(40,40,40));
                //circle(mRgba, new Point(x,y),10, new Scalar(0,0,255),-1);
            }
        }
        return mRgba;
    }

    private void ChooseColorFromMiddle() {
        int cols = mRgba.cols();
        int rows = mRgba.rows();
        int x = cols/2;
        int y = rows/2;
        Rect touchedRect = new Rect();
        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;
        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;
        Mat touchedRegionRgba = mRgba.submat(touchedRect);
        Mat touchedRegionHsv = new Mat();
        cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;
        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
        mDetector.setHsvColor(mBlobColorHsv);
        Imgproc.resize(mDetector.getSpectrum(), new Mat(), SPECTRUM_SIZE);
        touchedRegionRgba.release();
        touchedRegionHsv.release();
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
}