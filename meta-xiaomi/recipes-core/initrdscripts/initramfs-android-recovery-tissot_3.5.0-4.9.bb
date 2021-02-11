require recipes-core/initrdscripts/initramfs-android-recovery.inc

COMPATIBLE_MACHINE = "^tissot$"
RECOVERY_IMG = "recovery-3.5.0_9-0-tissot.img"
SRC_URI = "https://gitlab.com/Tofe/recovery-tissot/-/raw/halium-9.0/${RECOVERY_IMG}"
SRC_URI[md5sum] = "04496460e4ca579784307725af947461"
SRC_URI[sha256sum] = "e6d76726566fc4c9cd1c2ec832492dca94884d2a0b1b3b6b1c3f68fd68dfa1ec"
INSANE_SKIP_${PN} = "arch already-stripped debug-files"
# NOTE: Executing RunQueue Tasks ERROR: initramfs-android-recovery-tissot-3.1.1-1-r0 do_package_qa: QA Issue: /recovery/sbin/permissive.sh contained in package initramfs-android-recovery-tissot requires /sbin/sh, but no providers found in RDEPENDS_initramfs-android-recovery-tissot? [file-rdeps]
INSANE_SKIP_${PN} += "file-rdeps"
