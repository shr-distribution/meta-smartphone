/*
 * Stand-in for mako's /vendor/bin/mm-qcamera-daemon (Android 9 blob).
 *
 * The blob's main() only calls qcamsvr_start() from liboemcamera.so and returns -1 when that is
 * negative, 0 otherwise. liboemcamera.so needs libmmjpeg.so, which has text relocations (ten
 * literal-pool words in its idct_func section). The Android 16 linker refuses a library with text
 * relocations for a process that targets API 23 or newer, and an executable always does, so the
 * blob dies with "CANNOT LINK EXECUTABLE" before main() and init restarts it every 5 s for ever.
 *
 * Loading the same libraries with dlopen() after the target SDK version is lowered to 22 lets the
 * linker relocate them as Android 9 did. The setter is the linker's own export in ld-android.so,
 * which the linker puts in every namespace. (libpermissioncache_shim.so lowers it from a
 * constructor too, but only when preloaded: dlopen()ed, its weak reference stays unresolved.)
 */
#include <dlfcn.h>
#include <android/log.h>

#define TAG "qcamsvr-launcher"

void __loader_android_set_application_target_sdk_version(int target);

static void *load(const char *name)
{
    void *h = dlopen(name, RTLD_NOW | RTLD_GLOBAL);
    if (!h)
        __android_log_print(ANDROID_LOG_ERROR, TAG, "dlopen %s: %s", name, dlerror());
    return h;
}

int main(void)
{
    __loader_android_set_application_target_sdk_version(22);
    void *oem = load("liboemcamera.so");
    if (!oem)
        return 1;
    int (*start)(void) = (int (*)(void))dlsym(oem, "qcamsvr_start");
    if (!start) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "dlsym qcamsvr_start: %s", dlerror());
        return 1;
    }
    return start() < 0 ? -1 : 0;
}
