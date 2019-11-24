#include "player_native_methods.h"
#include "player_java_bindings.h"

// Unregister this thread from the VM
static void detach_current_thread (void *env)
{
    (*java_vm)->DetachCurrentThread (java_vm);
}

// Library initializer
jint JNI_OnLoad (JavaVM * vm, void *reserved)
{
    JNIEnv *env = NULL;

    java_vm = vm;

    if ((*vm)->GetEnv (vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        __android_log_print (ANDROID_LOG_ERROR, "Player",
                             "Could not retrieve JNIEnv");
        return 0;
    }
    jclass klass = (*env)->FindClass (env, "org/freedesktop/gstreamer/Player");
    if (!klass) {
        __android_log_print (ANDROID_LOG_ERROR, "Player",
                             "Could not retrieve class org.freedesktop.gstreamer.Player");
        return 0;
    }
    if ((*env)->RegisterNatives (env, klass, native_methods,
                                 G_N_ELEMENTS (native_methods))) {
        __android_log_print (ANDROID_LOG_ERROR, "Player",
                             "Could not register native methods for org.freedesktop.gstreamer.Player");
        return 0;
    }

    pthread_key_create (&current_jni_env, detach_current_thread);

    return JNI_VERSION_1_6;
}
