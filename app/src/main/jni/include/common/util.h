//Android includes
#ifdef ANDROID
#include <android/log.h>
#include <jni.h>
#define LOG(prio, tag, a, args...) __android_log_print(prio, tag, "[%s::%d]"#a"",__FUNCTION__, __LINE__, ##args);
#define LOG_ALWAYS(...) ((void)__android_log_print(ANDROID_LOG_INFO, "FITCAM360", __VA_ARGS__))

#ifdef ENABLE_MUTE
#define LOG_SILENT(...) ((void)__android_log_print(ANDROID_LOG_INFO, "FITCAM360", __VA_ARGS__))
#else
#define LOG_SILENT(...) {}
#endif //ENABLE_MUTE

#else
#define LOG(prio, tag, a, args...) (printf(#prio, #tag, #a, __VA_ARGS__))
#define LOG_ALWAYS(...) { (printf(__VA_ARGS__)); \
                                   printf("\n"); }

#ifdef ENABLE_MUTE
#define LOG_SILENT(...) { (printf(__VA_ARGS__)); \
                                   printf("\n"); }
#else
#define LOG_SILENT(...) {}
#endif //ENABLE_MUTE

#endif //ANDROID
