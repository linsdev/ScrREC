#ifndef PLAYER_H
#define PLAYER_H

#include <jni.h>
#include <pthread.h>
#include <android/log.h>
#include <gst/player/player.h>

#define GET_CUSTOM_DATA(env, thiz, fieldID) (Player *)(gintptr)(*env)->GetLongField (env, thiz, fieldID)
#define SET_CUSTOM_DATA(env, thiz, fieldID, data) (*env)->SetLongField (env, thiz, fieldID, (jlong)(gintptr)data)

static pthread_key_t current_jni_env;
static JavaVM *java_vm;

#endif // PLAYER_H
