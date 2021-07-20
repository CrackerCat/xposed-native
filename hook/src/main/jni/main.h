//
// Created by Administrator on 2020-08-20.
//

#ifndef FENXIANG_MAIN_H
#define FENXIANG_MAIN_H


#include <jni.h>
#include <dlfcn.h>
#include <android/log.h>
#include <string.h>
#include <malloc.h>
#include <stdbool.h>
#include "Hook/HLua.h"
#include <stdlib.h>
#include "utils/parse.h"
#include <stdio.h>
#include "inlineHook/inlineHook.h"
#include "dlfc/dlfcn_compat.h"
#include "dlfc/dlfcn_nougat.h"
#include "utils/Log.h"
#include "utils/logging.h"

jstring  (*Source_Java_com_huruwo_behook_MainActivity_md5)(JNIEnv *env, jobject thiz, jstring str) = nullptr;


#endif //FENXIANG_MAIN_H
