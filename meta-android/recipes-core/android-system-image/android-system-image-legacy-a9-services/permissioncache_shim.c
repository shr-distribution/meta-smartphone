/*
 * android::PermissionCache::checkPermission(String16 const&, int pid, unsigned uid)
 *
 * Android 9's libmedia.so and libgui.so call this, and it lives in the full libbinder.so.
 * The VNDK-28 snapshot's libbinder does not export it, so a vendor that still links
 * those two framework libraries (the pre-Treble msm8974 hwcomposer does, through
 * libmedia) cannot be loaded. This grants every request. It is only ever preloaded into
 * the composer HAL service, where no permission is being enforced for anyone.
 *
 * It also provides __system_properties_init@LIBC_PLATFORM: old A9 libraries
 * (libpdx_default_transport, pulled in through libgui) import it under that version,
 * while the A16 libc defines it as __system_properties_init@@LIBC_Q, so their lookup
 * fails. The stub is a NON-default version (single @), so it only satisfies references
 * that ask for LIBC_PLATFORM. Do not make it a default or unversioned export: libc.so
 * is not linked -Bsymbolic, so libc's own start-up call would then bind to this stub,
 * the property area would never be initialised, __system_property_find() would return
 * NULL for everything and libhidl's WaitForProperty(hwservicemanager.ready) would spin
 * forever. A dlsym(RTLD_NEXT) forwarder hangs instead, dlsym is not usable during
 * libc's init.
 *
 * It also lowers the process's target SDK version to 22 from a constructor. Android 9's
 * Qualcomm JPEG library, libmmjpeg.so, has text relocations. The Android 16 linker refuses to
 * load such a library for a process that targets API 23 or newer ("has text relocations"),
 * which every native service does by default. The camera HAL's still-capture path then never
 * gets a JPEG encoder (OMX_GetHandle fails in qomx_image_core) and the capture request waits
 * for ever: the camera app locks up on the first photo. Below 23 the linker makes the segment
 * writable, relocates it and carries on, as the Android 9 one did.
 *
 * Plain C with the mangled name, so it needs no C++ runtime and no libc.
 */
__attribute__((visibility("default")))
unsigned char _ZN7android15PermissionCache15checkPermissionERKNS_8String16Eij(const void *permission,
                                                                             int pid, unsigned int uid)
{
    (void)permission; (void)pid; (void)uid;
    return 1;
}

/*
 * The composer HAL service puts itself on SCHED_FIFO at priority 2 in main(). On a
 * dual-core 3.4 device any thread of it that spins then starves the msm watchdog's pet
 * and the whole device resets. Keep it on the normal scheduler, whatever it asks for.
 */
__attribute__((visibility("default")))
int sched_setscheduler(int pid, int policy, const void *param)
{
    (void)pid; (void)policy; (void)param;
    return 0;
}

__attribute__((visibility("default")))
int shim_system_properties_init(void)
{
    return 0;
}
__asm__(".symver shim_system_properties_init,__system_properties_init@LIBC_PLATFORM");

extern void __loader_android_set_application_target_sdk_version(unsigned int version) __attribute__((weak));

__attribute__((constructor))
static void shim_allow_text_relocations(void)
{
    if (__loader_android_set_application_target_sdk_version)
        __loader_android_set_application_target_sdk_version(22);
}
