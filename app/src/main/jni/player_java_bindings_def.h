#ifndef PLAYER_JAVA_BINDINGS_DEF_H
#define PLAYER_JAVA_BINDINGS_DEF_H

#include <jni.h>
#include <android/native_window.h>

typedef struct _Player
{
    jobject java_player;
    GstPlayer *player;
    GstPlayerVideoRenderer *renderer;
    ANativeWindow *native_window;
} Player;

static void on_position_updated (GstPlayer * unused, GstClockTime position, Player * player);
static void on_duration_changed (GstPlayer * unused, GstClockTime duration, Player * player);
static void on_state_changed (GstPlayer * unused, GstPlayerState state, Player * player);
static void on_buffering (GstPlayer * unused, gint percent, Player * player);
static void on_end_of_stream (GstPlayer * unused, Player * player);
static void on_error (GstPlayer * unused, GError * err, Player * player);
static void on_video_dimensions_changed (GstPlayer * unused, gint width, gint height, Player * player);

static jmethodID on_position_updated_method_id;
static jmethodID on_duration_changed_method_id;
static jmethodID on_state_changed_method_id;
static jmethodID on_buffering_method_id;
static jmethodID on_end_of_stream_method_id;
static jmethodID on_error_method_id;
static jmethodID on_video_dimensions_changed_method_id;

#endif // PLAYER_JAVA_BINDINGS_DEF_H
