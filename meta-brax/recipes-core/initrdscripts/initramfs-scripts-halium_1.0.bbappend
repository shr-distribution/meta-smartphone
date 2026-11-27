FILESEXTRAPATHS:prepend := "${THISDIR}/initramfs-scripts-halium:"

# iodeOS's recovery is a user build: it installs only packages signed by a key in
# /system/etc/security/otacerts.zip. That file is in the vendor ramdisk, and ours
# is unpacked after it, so a copy here replaces it. It keeps the two certificates
# iodeOS ships, so its own packages still install, and adds the one that
# luneos-recovery-package is signed with. Android never sees this file: the
# initramfs is gone once the system runs.
SRC_URI:append:brax3-iode = " \
    file://otacerts/lineage.x509.pem \
    file://otacerts/releasekey.x509.pem \
    file://otacerts/luneos-recovery.x509.pem \
"

do_install:append:brax3-iode() {
    install -d ${D}/system/etc/security
    python3 - ${UNPACKDIR}/otacerts ${D}/system/etc/security/otacerts.zip <<'PYEOF'
import os, sys, zipfile
src, out = sys.argv[1:]
with zipfile.ZipFile(out, 'w', zipfile.ZIP_DEFLATED) as z:
    for name in sorted(os.listdir(src)):
        info = zipfile.ZipInfo(name, (2008, 1, 1, 0, 0, 0))
        info.compress_type = zipfile.ZIP_DEFLATED
        info.external_attr = 0o644 << 16
        z.writestr(info, open(os.path.join(src, name), 'rb').read())
PYEOF
    chmod 0644 ${D}/system/etc/security/otacerts.zip
}

FILES:${PN}:append:brax3-iode = " /system/etc/security/otacerts.zip"
