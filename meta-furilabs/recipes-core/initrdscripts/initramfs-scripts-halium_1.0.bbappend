FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

COMPATIBLE_MACHINE:radon = "^radon$"

# The LVM/bootman premount script - see the file for the whole contract.
# Installed the same way tenderloin installs its own: into
# /scripts/local-premount with a line appended to ORDER, which is what
# functions' run_scripts() sources at boot.
SRC_URI:append:radon = " file://S01-lvm-bootman.sh file://init-trace-wrapper.sh"

do_install:append:radon() {
    install -d ${D}/scripts/local-premount
    install -m 0755 ${UNPACKDIR}/S01-lvm-bootman.sh \
        ${D}/scripts/local-premount/S01-lvm-bootman.sh
    echo ". /scripts/local-premount/S01-lvm-bootman.sh" >> ${D}/scripts/local-premount/ORDER
}

# Opt-in initramfs trace harness, off by default.
#
#   MACHINE=radon LUNEOS_TRACE_INITRD=1 bitbake linux-furilabs-radon
#
# Wraps /init so the whole boot runs under xtrace, streamed to the unused
# init_boot_b partition and readable afterwards with mtkclient. See the wrapper
# for why this device needs it: no UART, no USB gadget, black panel and an empty
# pstore log, which left every bring-up test as a single bit of information.
#
# Read back with:
#   ( cd ~/mtkclient && python3 mtk.py r init_boot_b trace.bin )
#   strings trace.bin | sed -n '/LUNEOS-TRACE-BEGIN/,$p'
#
# Like LUNEOS_ENABLE_ADB this is read inside a task, so bitbake's signature
# changes with it and the image rebuilds on its own.
LUNEOS_TRACE_INITRD ??= "0"

do_install:append:radon() {
    if [ "${LUNEOS_TRACE_INITRD}" = "1" ]; then
        mv ${D}/init ${D}/init.real
        install -m 0755 ${UNPACKDIR}/init-trace-wrapper.sh ${D}/init
    fi
}

# /init.real is the original init, moved aside by the trace harness above. It
# has to be listed or do_package fails the build with "installed and not
# shipped" - and it is listed unconditionally rather than inside the
# LUNEOS_TRACE_INITRD guard because FILES is parsed, not executed, so it cannot
# depend on a shell conditional. Naming a path that does not exist is harmless.
FILES:${PN}:append:radon = " /scripts/local-premount /init.real"
