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

#include <jni.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include <opencv2/imgproc.hpp>

using namespace std;
using namespace cv;

//to prevent Name mangling in C++ (for JNI connectivity)
#ifdef __cplusplus
extern "C" {
#endif

Mat * mCanny = NULL;

void ConvertYUVtoBGRA(unsigned char *src, unsigned char *dest, int width, int height)
{
  cv::Mat myuv(height + height/2, width, CV_8UC1, src);
  cv::Mat mrgb(height, width, CV_8UC4, dest);
  cv::cvtColor(myuv, mrgb, CV_YUV2BGRA_NV21);
  return;
}

jboolean Java_com_mrlab_NativeCameraProcessing_CameraPreview_ImageProcessing(
        JNIEnv* env, jobject thiz,
        jint width, jint height,
        jbyteArray NV21FrameData, jintArray outPixels, jboolean bRotate)
{
  jbyte * pNV21FrameData = env->GetByteArrayElements(NV21FrameData, 0);
  jint * poutPixels = env->GetIntArrayElements(outPixels, 0);

  /*
  // Canny Edge Example
  if ( mCanny == NULL )
  {
    mCanny = new Mat(height, width, CV_8UC1);
  }

  Mat mGray(height, width, CV_8UC1, (unsigned char *)pNV21FrameData);
  Mat mResult(height, width, CV_8UC4, (unsigned char *)poutPixels);
  IplImage srcImg = mGray;
  IplImage CannyImg = *mCanny;
  IplImage ResultImg = mResult;

  cvCanny(&srcImg, &CannyImg, 80, 100, 3);
  cvCvtColor(&CannyImg, &ResultImg, CV_GRAY2BGRA);
  */


  // Image Duplication Example
  if(bRotate) {
    Mat mResult(width, height, CV_8UC4, (unsigned char *)poutPixels);
    Mat mDecoded(height, width, CV_8UC4);
    ConvertYUVtoBGRA((unsigned char *)pNV21FrameData, (unsigned char *)mDecoded.data, width, height);
    // Rotated image toware CW 90 degree
    transpose(mDecoded, mResult);
    flip(mResult, mResult,1); //transpose+flip(1)=CW
  } else {
    ConvertYUVtoBGRA((unsigned char *)pNV21FrameData, (unsigned char *)poutPixels, width, height);
  }

  // Memory Release
  env->ReleaseByteArrayElements(NV21FrameData, pNV21FrameData, 0);
  env->ReleaseIntArrayElements(outPixels, poutPixels, 0);

  return true;
}

#ifdef __cplusplus
}
#endif

