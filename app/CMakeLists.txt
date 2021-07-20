
# Sets the minimum version of CMake required to build the native library.
#指定 需要CMAKE的 最小版本要求
cmake_minimum_required(VERSION 3.4.1)

##引入加密库OpenSSL，静态链接库
#INCLUDE_DIRECTORIES(
#        src/main/cpp/openssllib/include/openssl/
#)
#LINK_DIRECTORIES(src/main/cpp/openssllib/${ANDROID_ABI}/)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.


#添加一个 远程链接库
add_library( # Sets the name of the library. (我们用C/C++ 文件生成的 动态库 so名字 )
        Test

        # Sets the library as a shared library.(表明用于共享的 动态库 so结尾)
        SHARED


        src/main/jni/main.cpp
        src/main/jni/encrypt/MyMD5.cpp
        src/main/jni/utils/parse.cpp

        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.（调用系统的日志库）
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library. 链接 我们自己生成的库 名字要和add_library 一样
        Test

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})