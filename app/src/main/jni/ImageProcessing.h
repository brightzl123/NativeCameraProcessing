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

#ifndef _IMAGE_PROCESSING_H_
#define _IMAGE_PROCESSING_H_

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

jboolean Java_com_mrlab_NativeCameraProcessing_CameraPreview_ImageProcessing(
        JNIEnv* env, jobject thiz,
        jint width, jint height,
        jbyteArray NV21FrameData, jintArray outPixels, jboolean bRotate);
#ifdef __cplusplus
}
#endif

#endif //_IMAGE_PROCESSING_H_

