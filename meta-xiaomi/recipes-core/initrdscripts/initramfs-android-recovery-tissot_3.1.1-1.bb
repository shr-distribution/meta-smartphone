require recipes-core/initrdscripts/initramfs-android-recovery.inc

COMPATIBLE_MACHINE = "^tissot$"
RECOVERY_IMG = "recovery-3.1.1-1.img"
SRC_URI = "https://gitlab.com/nizovn/recovery-tissot/raw/master/${RECOVERY_IMG}"
SRC_URI[md5sum] = "6117790563a47026be46aca6265994b7"
SRC_URI[sha256sum] = "04002c2d8f4715f2678eb574cbb5ff10e7b93f8b3282f616f66af3dd6cd7941f"
INSANE_SKIP_${PN} = "arch already-stripped debug-files"
# NOTE: Executing RunQueue Tasks ERROR: initramfs-android-recovery-tissot-3.1.1-1-r0 do_package_qa: QA Issue: /recovery/sbin/permissive.sh contained in package initramfs-android-recovery-tissot requires /sbin/sh, but no providers found in RDEPENDS_initramfs-android-recovery-tissot? [file-rdeps]
INSANE_SKIP_${PN} += "file-rdeps"
