/*******************************************************
 Copyright 2016 Yongjin Kim <ladinjin@gmail.com>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 *******************************************************/

package com.mrlab.NativeCameraProcessing;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback
{
    private Context ctx = null;
    private Camera mCamera = null;
    private ImageView MyCameraPreview = null;
    private Bitmap bitmap = null;
    private int[] pixels = null;
    private byte[] FrameData = null;
    private int imageFormat;
    private int orgPreviewWidth;
    private int orgPreviewHeight;
    private boolean bProcessing = false;
    private int orientation;

    Handler mHandler = new Handler(Looper.getMainLooper());

    public CameraPreview(Context context, ImageView CameraPreview)
    {
        ctx = context;
        MyCameraPreview = CameraPreview;
    }

    @Override
    public void onPreviewFrame(byte[] arg0, Camera arg1)
    {
        // At preview mode, the frame data will push to here.
        if (imageFormat == ImageFormat.NV21)
        {
            //We only accept the NV21(YUV420) format.
            if ( !bProcessing )
            {
                FrameData = arg0;
                mHandler.post(DoImageProcessing);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
    {
        Camera.Parameters parameters = mCamera.getParameters();

        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : previewSizes) {
            Log.i("Supported resolution", "|" + size.width + "x" + size.height);
        }
        Camera.Size size = previewSizes.get(0);
        orgPreviewWidth = size.width;
        orgPreviewHeight = size.height;

        orientation = ctx.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mCamera.setDisplayOrientation(90);
            bitmap = Bitmap.createBitmap(orgPreviewHeight, orgPreviewWidth, Bitmap.Config.ARGB_8888);
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mCamera.setDisplayOrientation(0);
            bitmap = Bitmap.createBitmap(orgPreviewWidth, orgPreviewHeight, Bitmap.Config.ARGB_8888);
        }
        pixels = new int[orgPreviewWidth * orgPreviewHeight];

        imageFormat = parameters.getPreviewFormat();
        parameters.setPreviewSize(size.width, size.height);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0)
    {
        mCamera = Camera.open();
        try
        {
            // If did not set the SurfaceHolder, the preview area will be black.
            mCamera.setPreviewDisplay(arg0);
            mCamera.setPreviewCallback(this);
        }
        catch (IOException e)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0)
    {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    static public byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight)
    {
        byte [] yuv = new byte[imageWidth*imageHeight*3/2];
        // Rotate the Y luma
        int i = 0;
        for(int x = 0;x < imageWidth;x++)
        {
            for(int y = imageHeight-1;y >= 0;y--)
            {
                yuv[i] = data[y*imageWidth+x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth*imageHeight*3/2-1;
        for(int x = imageWidth-1;x > 0;x=x-2)
        {
            for(int y = 0;y < imageHeight/2;y++)
            {
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
                i--;
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
                i--;
            }
        }
        return yuv;
    }

    // Native JNI part
    public native boolean ImageProcessing(int width, int height, byte[] NV21FrameData, int [] pixels, boolean bRotate);
    static
    {
        System.loadLibrary("native");
    }

    private Runnable DoImageProcessing = new Runnable()
    {
        public void run()
        {
            bProcessing = true;

            //byte[] rotatedArray = CameraPreview.rotateYUV420Degree90(FrameData, orgPreviewWidth, orgPreviewHeight);
            //ImageProcessing(orgPreviewHeight, orgPreviewWidth, rotatedArray, pixels);

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                ImageProcessing(orgPreviewWidth, orgPreviewHeight, FrameData, pixels, true);
                bitmap.setPixels(pixels, 0, orgPreviewHeight, 0, 0, orgPreviewHeight, orgPreviewWidth);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ImageProcessing(orgPreviewWidth, orgPreviewHeight, FrameData, pixels, false);
                bitmap.setPixels(pixels, 0, orgPreviewWidth, 0, 0, orgPreviewWidth, orgPreviewHeight);
            }

            MyCameraPreview.setImageBitmap(bitmap);
            bProcessing = false;
        }
    };
}