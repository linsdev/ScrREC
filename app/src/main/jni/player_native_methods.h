#ifndef PLAYER_NATIVE_METHODS_H
#define PLAYER_NATIVE_METHODS_H

#include <android/native_window_jni.h>

#include "player.h"
#include "player_native_methods_def.h"
#include "player_java_bindings_def.h"

static jfieldID native_player_field_id;

static void native_new (JNIEnv * env, jobject thiz)
{
    Player *player = g_new0 (Player, 1);

    player->renderer = gst_player_video_overlay_video_renderer_new (NULL);
    player->player = gst_player_new (player->renderer, NULL);
    SET_CUSTOM_DATA (env, thiz, native_player_field_id, player);
    player->java_player = (*env)->NewGlobalRef (env, thiz);

    g_signal_connect (player->player, "position-updated",
                      G_CALLBACK (on_position_updated), player);
    g_signal_connect (player->player, "duration-changed",
                      G_CALLBACK (on_duration_changed), player);
    g_signal_connect (player->player, "state-changed",
                      G_CALLBACK (on_state_changed), player);
    g_signal_connect (player->player, "buffering",
                      G_CALLBACK (on_buffering), player);
    g_signal_connect (player->player, "end-of-stream",
                      G_CALLBACK (on_end_of_stream), player);
    g_signal_connect (player->player, "error", G_CALLBACK (on_error), player);
    g_signal_connect (player->player, "video-dimensions-changed",
                      G_CALLBACK (on_video_dimensions_changed), player);
}

static void native_free (JNIEnv * env, jobject thiz)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);

    if (!player)
        return;

    g_object_unref (player->player);
    (*env)->DeleteGlobalRef (env, player->java_player);
    g_free (player);
    SET_CUSTOM_DATA (env, thiz, native_player_field_id, NULL);
}

static void native_play (JNIEnv * env, jobject thiz)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);

    if (!player)
        return;

    gst_player_play (player->player);
}

static void native_pause (JNIEnv * env, jobject thiz)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);

    if (!player)
        return;

    gst_player_pause (player->player);
}

static void native_stop (JNIEnv * env, jobject thiz)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);

    if (!player)
        return;

    gst_player_stop (player->player);
}

static void native_seek (JNIEnv * env, jobject thiz, jlong position)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);

    if (!player)
        return;

    gst_player_seek (player->player, position);
}

static void native_set_uri (JNIEnv * env, jobject thiz, jobject uri)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);
    const gchar *uri_str;

    if (!player)
        return;

    uri_str = (*env)->GetStringUTFChars (env, uri, NULL);
    g_object_set (player->player, "uri", uri_str, NULL);
    (*env)->ReleaseStringUTFChars (env, uri, uri_str);
}

static jobject native_get_uri (JNIEnv * env, jobject thiz)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);
    jobject uri;
    gchar *uri_str;

    if (!player)
        return NULL;

    g_object_get (player->player, "uri", &uri_str, NULL);

    uri = (*env)->NewStringUTF (env, uri_str);
    g_free (uri_str);

    return uri;
}

static jlong native_get_position (JNIEnv * env, jobject thiz)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);
    jdouble position;

    if (!player)
        return -1;

    g_object_get (player->player, "position", &position, NULL);

    return position;
}

static jlong native_get_duration (JNIEnv * env, jobject thiz)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);
    jlong duration;

    if (!player)
        return -1;

    g_object_get (player->player, "duration", &duration, NULL);

    return duration;
}

static jdouble native_get_volume (JNIEnv * env, jobject thiz)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);
    jdouble volume;

    if (!player)
        return 1.0;

    g_object_get (player->player, "volume", &volume, NULL);

    return volume;
}

static void native_set_volume (JNIEnv * env, jobject thiz, jdouble volume)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);

    if (!player)
        return;

    g_object_set (player->player, "volume", volume, NULL);
}

static jboolean native_get_mute (JNIEnv * env, jobject thiz)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);
    jboolean mute;

    if (!player)
        return FALSE;

    g_object_get (player->player, "mute", &mute, NULL);

    return mute;
}

static void native_set_mute (JNIEnv * env, jobject thiz, jboolean mute)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);

    if (!player)
        return;

    g_object_set (player->player, "mute", mute, NULL);
}

static void native_set_surface (JNIEnv * env, jobject thiz, jobject surface)
{
    Player *player = GET_CUSTOM_DATA (env, thiz, native_player_field_id);
    ANativeWindow *new_native_window;

    if (!player)
        return;

    new_native_window = surface ? ANativeWindow_fromSurface (env, surface) : NULL;

    if (player->native_window) {
        ANativeWindow_release (player->native_window);
    }

    player->native_window = new_native_window;
    gst_player_video_overlay_video_renderer_set_window_handle
            (GST_PLAYER_VIDEO_OVERLAY_VIDEO_RENDERER (player->renderer),
             (gpointer) new_native_window);
}

static void native_class_init (JNIEnv * env, jclass klass)
{
    native_player_field_id =
            (*env)->GetFieldID (env, klass, "native_player", "J");
    on_position_updated_method_id =
            (*env)->GetMethodID (env, klass, "onPositionUpdated", "(J)V");
    on_duration_changed_method_id =
            (*env)->GetMethodID (env, klass, "onDurationChanged", "(J)V");
    on_state_changed_method_id =
            (*env)->GetMethodID (env, klass, "onStateChanged", "(I)V");
    on_buffering_method_id =
            (*env)->GetMethodID (env, klass, "onBuffering", "(I)V");
    on_end_of_stream_method_id =
            (*env)->GetMethodID (env, klass, "onEndOfStream", "()V");
    on_error_method_id =
            (*env)->GetMethodID (env, klass, "onError", "(ILjava/lang/String;)V");
    on_video_dimensions_changed_method_id =
            (*env)->GetMethodID (env, klass, "onVideoDimensionsChanged", "(II)V");

    if (!native_player_field_id ||
        !on_position_updated_method_id || !on_duration_changed_method_id ||
        !on_state_changed_method_id || !on_buffering_method_id ||
        !on_end_of_stream_method_id ||
        !on_error_method_id || !on_video_dimensions_changed_method_id) {
        const gchar *message =
                "The calling class does not implement all necessary interface methods";
        jclass exception_class = (*env)->FindClass (env, "java/lang/Exception");
        __android_log_print (ANDROID_LOG_ERROR, "Player", "%s", message);
        (*env)->ThrowNew (env, exception_class, message);
    }

    gst_debug_set_threshold_for_name ("player", GST_LEVEL_TRACE);
}

#endif // PLAYER_NATIVE_METHODS_H
