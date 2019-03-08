#!/bin/sh

# With stage 025 the rootfs directory name has changed from "webos" to "luneos"
stage_025_new_rootfs_dir_name() {
    if [ -d $ANDROID_SDCARD_DIR/webos ] ; then
        mv $ANDROID_SDCARD_DIR/webos $ANDROID_SDCARD_DIR/$distro_name
    fi
    if [ -d $ANDROID_SDCARD_DIR/webos-data ] ; then
        mv $ANDROID_SDCARD_DIR/webos-data $ANDROID_SDCARD_DIR/$distro_name-data
    fi
}

stage_025_new_rootfs_dir_name
