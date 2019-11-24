#ifndef PLAYER_NATIVE_METHODS_DEF_H
#define PLAYER_NATIVE_METHODS_DEF_H

#include <jni.h>

#define GET_CUSTOM_DATA(env, thiz, fieldID) (Player *)(gintptr)(*env)->GetLongField (env, thiz, fieldID)
#define SET_CUSTOM_DATA(env, thiz, fieldID, data) (*env)->SetLongField (env, thiz, fieldID, (jlong)(gintptr)data)

static void native_class_init (JNIEnv * env, jclass klass);
static void native_new (JNIEnv * env, jobject thiz);
static void native_free (JNIEnv * env, jobject thiz);
static void native_play (JNIEnv * env, jobject thiz);
static void native_pause (JNIEnv * env, jobject thiz);
static void native_stop (JNIEnv * env, jobject thiz);
static void native_seek (JNIEnv * env, jobject thiz, jlong position);
static void native_set_uri (JNIEnv * env, jobject thiz, jobject uri);
static jobject native_get_uri (JNIEnv * env, jobject thiz);
static jlong native_get_position (JNIEnv * env, jobject thiz);
static jlong native_get_duration (JNIEnv * env, jobject thiz);
static jdouble native_get_volume (JNIEnv * env, jobject thiz);
static void native_set_volume (JNIEnv * env, jobject thiz, jdouble volume);
static jboolean native_get_mute (JNIEnv * env, jobject thiz);
static void native_set_mute (JNIEnv * env, jobject thiz, jboolean mute);
static void native_set_surface (JNIEnv * env, jobject thiz, jobject surface);

// List of implemented native methods
static JNINativeMethod native_methods[] = {
        {"nativeClassInit", "()V", (void *) native_class_init},
        {"nativeNew", "()V", (void *) native_new},
        {"nativePlay", "()V", (void *) native_play},
        {"nativePause", "()V", (void *) native_pause},
        {"nativeStop", "()V", (void *) native_stop},
        {"nativeSeek", "(J)V", (void *) native_seek},
        {"nativeFree", "()V", (void *) native_free},
        {"nativeGetUri", "()Ljava/lang/String;", (void *) native_get_uri},
        {"nativeSetUri", "(Ljava/lang/String;)V", (void *) native_set_uri},
        {"nativeGetPosition", "()J", (void *) native_get_position},
        {"nativeGetDuration", "()J", (void *) native_get_duration},
        {"nativeGetVolume", "()D", (void *) native_get_volume},
        {"nativeSetVolume", "(D)V", (void *) native_set_volume},
        {"nativeGetMute", "()Z", (void *) native_get_mute},
        {"nativeSetMute", "(Z)V", (void *) native_set_mute},
        {"nativeSetSurface", "(Landroid/view/Surface;)V",
                (void *) native_set_surface}
};

#endif // PLAYER_NATIVE_METHODS_DEF_H
