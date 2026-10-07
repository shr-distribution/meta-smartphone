SUMMARY = "Symbols old vendor libraries expect from the full libbinder and libmedia"
DESCRIPTION = "A freestanding shared library that halium-legacy-vendor adds as a dependency of the \
Android 9/11 framework copies a legacy vendor brings along, so that they load against the VNDK snapshot \
of the 16.0 GSI."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

# The stubs that return an object by value rely on the 32-bit ARM hidden return pointer in r0.
COMPATIBLE_HOST = "arm.*-linux"

SRC_URI = "file://halium_legacy_shim.c"

S = "${UNPACKDIR}"

# No libc, no C++ runtime, nothing to link: the library is only looked up by the vendor's own loader.
do_compile() {
    ${CC} ${CFLAGS} -O2 -fPIC -ffreestanding -fno-stack-protector -fvisibility=hidden \
        -nostdlib -shared -Wl,-soname,libhalium_legacy_shim.so -Wl,--hash-style=gnu \
        -o ${B}/libhalium_legacy_shim.so ${UNPACKDIR}/halium_legacy_shim.c
}

# Kept out of the usual library directory and out of every image: only the converter reads it, from the
# sysroot, and puts a copy into the vendor image.
do_install() {
    install -d ${D}${libdir}/halium-legacy
    install -m 0644 ${B}/libhalium_legacy_shim.so ${D}${libdir}/halium-legacy/libhalium_legacy_shim.so
}

FILES:${PN} = "${libdir}/halium-legacy/libhalium_legacy_shim.so"
FILES:${PN}-dev = ""
INSANE_SKIP:${PN} = "ldflags dev-so file-rdeps"
EXCLUDE_FROM_SHLIBS = "1"
