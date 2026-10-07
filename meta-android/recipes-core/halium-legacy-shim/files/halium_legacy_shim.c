/*
 * libhalium_legacy_shim.so: symbols that old vendor libraries expect and a vendor on the 16.0 GSI does not get.
 *
 * A vendor converted by halium-legacy-vendor can bring along Android 9/11 framework copies (libgui,
 * libmedia, libmediautils, libsensor, libandroid_runtime, ...) because its HALs link them. Those were built
 * against the full libbinder, libmedia and libstagefright_foundation, and the VNDK snapshot the GSI carries
 * for the vendor's release (the vendor variant of libbinder in particular) leaves the permission, AppOps and
 * Parcel helpers out. dlopen() with RTLD_NOW then fails on the first symbol it cannot resolve.
 *
 * Nothing here runs on the paths a HAL uses: the permission checks are the camera service's and the
 * media service's, neither of which is in the process. Every function answers "allowed" or "nothing"; one
 * that returns an object by value writes an empty one through the hidden return pointer in r0
 * (ARM EABI).
 *
 * The converter adds this library as a dependency of the vendor libraries that name it in
 * HALIUM_LEGACY_SHIM_TARGETS. It is plain C with the mangled C++ names, so it needs no C++ runtime and no
 * libc, and ARM only (the hidden return pointer is r0 here and x8 on arm64).
 *
 * Generated from the unresolved symbols of the SM-T520 camera chain; add a symbol when a device needs one.
 */
#define EXPORT __attribute__((visibility("default")))
#define NULLPTR ((void *)0)

EXPORT void _ZN7android21IPermissionController11asInterfaceERKNS_2spINS_7IBinderEEE(void **ret) { *ret = NULLPTR; }
EXPORT int _ZN7android13AppOpsManager18permissionToOpCodeERKNS_8String16E(void) { return -1; }
/*
 * AppOpsManager is { Mutex mLock; sp<IAppOpsService> mService; }: 4 + 4 bytes on 32-bit ARM (bionic's
 * pthread_mutex_t is one int32_t there, sp<> one pointer; Android 11 frameworks/native AppOpsManager.h). Callers
 * construct it on the stack (frameworks/av media/utils ServiceUtilities.cpp: "AppOpsManager appOps;") and run its
 * implicit, inlined destructor on it, which destroys the mutex and drops the sp<>. A constructor that leaves both
 * as stack garbage makes that a decStrong() on a garbage pointer, so zero them: a zeroed bionic mutex is
 * PTHREAD_MUTEX_INITIALIZER and a null sp<> is empty.
 */
EXPORT void _ZN7android13AppOpsManagerC1Ev(unsigned *self) { self[0] = 0; self[1] = 0; }
EXPORT void _ZN7android13AppOpsManagerC2Ev(unsigned *self) { self[0] = 0; self[1] = 0; }
EXPORT unsigned char _ZN7android15PermissionCache15checkPermissionERKNS_8String16Eij(void) { return 1; }
EXPORT int _ZN7android12MetaDataBase16updateFromParcelERKNS_6ParcelE(void) { return 0; }
EXPORT void _ZN7android8AMessage10FromParcelERKNS_6ParcelEj(void **ret) { *ret = NULLPTR; }
EXPORT void _ZN7android8MetaData16createFromParcelERKNS_6ParcelE(void **ret) { *ret = NULLPTR; }
EXPORT int _ZN7android12MetaDataBase13writeToParcelERNS_6ParcelE(void) { return 0; }
EXPORT int _ZNK7android8AMessage13writeToParcelEPNS_6ParcelE(void) { return 0; }
EXPORT void _ZN7android7AString10FromParcelERKNS_6ParcelE(unsigned *ret) { ret[0] = 0; ret[1] = 0; ret[2] = 0; }
EXPORT int _ZNK7android7AString13writeToParcelEPNS_6ParcelE(void) { return 0; }
EXPORT void _ZN7android13IBatteryStats11asInterfaceERKNS_2spINS_7IBinderEEE(void **ret) { *ret = NULLPTR; }
EXPORT unsigned char _ZN7android15PermissionCache22checkCallingPermissionERKNS_8String16E(void) { return 1; }
EXPORT unsigned char _ZN7android20PermissionController15checkPermissionERKNS_8String16Eii(void) { return 1; }
EXPORT int _ZN7android13AppOpsManager14startOpNoThrowEiiRKNS_8String16Eb(void) { return 0; }
EXPORT int _ZN7android20PermissionController17getPackagesForUidEjRNS_6VectorINS_8String16EEE(void) { return 0; }
EXPORT void _ZN7android13AppOpsManager8finishOpEiiRKNS_8String16E(void) { }
EXPORT void _ZN7android20PermissionControllerC1Ev(void) { }
EXPORT void _ZN7android20PermissionControllerC2Ev(void) { }
EXPORT int _ZN7android13AppOpsManager7checkOpEiiRKNS_8String16E(void) { return 0; }
EXPORT void _ZN7android19IProcessInfoService11asInterfaceERKNS_2spINS_7IBinderEEE(void **ret) { *ret = NULLPTR; }

/*
 * libandroid_runtime.so (pulled in under libhwui/libandroid) imports the process-group helpers of
 * libprocessgroup, the qtaguid socket tagging of libnetd_client and two JNI registration entry points
 * that the VNDK 30 libraries do not export; libmediautils imports a Scudo call. None of them runs in the
 * camera provider.
 */
EXPORT void DropTaskProfilesResourceCaching(void) { }
EXPORT int UsePerAppMemcg(void) { return 0; }
EXPORT int createProcessGroup(void) { return 0; }
EXPORT int killProcessGroup(void) { return 0; }
EXPORT int removeAllProcessGroups(void) { return 0; }
EXPORT int qtaguid_tagSocket(void) { return 0; }
EXPORT int qtaguid_untagSocket(void) { return 0; }
EXPORT int qtaguid_setCounterSet(void) { return 0; }
EXPORT int qtaguid_deleteTagData(void) { return 0; }
EXPORT int register_android_functions(void) { return 0; }
EXPORT int register_localized_collators(void) { return 0; }
EXPORT void __scudo_set_rss_limit(void) { }
