#ifndef PLAYER_JAVA_BINDINGS_H
#define PLAYER_JAVA_BINDINGS_H

#include "player.h"
#include "player_java_bindings_def.h"

// Register this thread with the VM
static JNIEnv * attach_current_thread (void)
{
    JNIEnv *env;
    JavaVMAttachArgs args;

    args.version = JNI_VERSION_1_6;
    args.name = NULL;
    args.group = NULL;

    if ((*java_vm)->AttachCurrentThread (java_vm, &env, &args) < 0)
        return NULL;

    return env;
}

// Retrieve the JNI environment for this thread
static JNIEnv * get_jni_env (void)
{
    JNIEnv *env = (JNIEnv *)pthread_getspecific (current_jni_env);

    if (env == NULL) {
        env = attach_current_thread ();
        pthread_setspecific (current_jni_env, env);
    }

    return env;
}

// Java Bindings

static void on_position_updated (GstPlayer * unused, GstClockTime position, Player * player)
{
    JNIEnv *env = get_jni_env ();

    (*env)->CallVoidMethod (env, player->java_player,
                            on_position_updated_method_id, position);
    if ((*env)->ExceptionCheck (env)) {
        (*env)->ExceptionDescribe (env);
        (*env)->ExceptionClear (env);
    }
}

static void on_duration_changed (GstPlayer * unused, GstClockTime duration, Player * player)
{
    JNIEnv *env = get_jni_env ();

    (*env)->CallVoidMethod (env, player->java_player,
                            on_duration_changed_method_id, duration);
    if ((*env)->ExceptionCheck (env)) {
        (*env)->ExceptionDescribe (env);
        (*env)->ExceptionClear (env);
    }
}

static void on_state_changed (GstPlayer * unused, GstPlayerState state, Player * player)
{
    JNIEnv *env = get_jni_env ();

    (*env)->CallVoidMethod (env, player->java_player,
                            on_state_changed_method_id, state);
    if ((*env)->ExceptionCheck (env)) {
        (*env)->ExceptionDescribe (env);
        (*env)->ExceptionClear (env);
    }
}

static void on_buffering (GstPlayer * unused, gint percent, Player * player)
{
    JNIEnv *env = get_jni_env ();

    (*env)->CallVoidMethod (env, player->java_player,
                            on_buffering_method_id, percent);
    if ((*env)->ExceptionCheck (env)) {
        (*env)->ExceptionDescribe (env);
        (*env)->ExceptionClear (env);
    }
}

static void on_end_of_stream (GstPlayer * unused, Player * player)
{
    JNIEnv *env = get_jni_env ();

    (*env)->CallVoidMethod (env, player->java_player, on_end_of_stream_method_id);
    if ((*env)->ExceptionCheck (env)) {
        (*env)->ExceptionDescribe (env);
        (*env)->ExceptionClear (env);
    }
}

static void on_error (GstPlayer * unused, GError * err, Player * player)
{
    JNIEnv *env = get_jni_env ();
    jstring error_msg;

    error_msg = (*env)->NewStringUTF (env, err->message);

    (*env)->CallVoidMethod (env, player->java_player, on_error_method_id,
                            err->code, error_msg);
    if ((*env)->ExceptionCheck (env)) {
        (*env)->ExceptionDescribe (env);
        (*env)->ExceptionClear (env);
    }

    (*env)->DeleteLocalRef (env, error_msg);
}

static void on_video_dimensions_changed (GstPlayer * unused, gint width, gint height, Player * player)
{
    JNIEnv *env = get_jni_env ();

    (*env)->CallVoidMethod (env, player->java_player,
                            on_video_dimensions_changed_method_id, width, height);
    if ((*env)->ExceptionCheck (env)) {
        (*env)->ExceptionDescribe (env);
        (*env)->ExceptionClear (env);
    }
}

#endif // PLAYER_JAVA_BINDINGS_H
