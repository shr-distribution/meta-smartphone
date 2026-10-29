# Module signing key for kernels built with CONFIG_MODULE_SIG_FORCE=y.
#
# No private key lives in this layer, or in any other. Until 2026-10 sargo and
# fajita pinned one committed PEM file
# (module-signing/luneos-module-signing-key.pem); that key is public, so treat
# it as burned: any kernel built with it accepts modules signed by anyone.
#
# Default: the kernel's own per-build key
# ---------------------------------------
# With MODULE_SIG_KEY unset, CONFIG_MODULE_SIG_KEY is the kbuild default
# "certs/signing_key.pem", which certs/Makefile generates in ${B} on the first
# build. kernel.bbclass do_shared_workdir copies certs/signing_key.* into
# STAGING_KERNEL_BUILDDIR, and module.bbclass's "make modules_install" signs
# out-of-tree modules (v4l2loopback, wireguard-module, ...) from there.
#
# The consequence: an out-of-tree module only loads on the kernel from the same
# build, because the next from-scratch kernel build generates a new key. Build
# kernel and modules together (one bitbake run, or both from the same sstate),
# and never install an old kernel-module-* package onto a newer kernel.
#
# Note that kbuild ignores a failed signature on an *external* module
# (scripts/Makefile.modinst: "|| true" when KBUILD_EXTMOD is set), so a
# missing key does not fail the build - it ships an unsigned module that a
# MODULE_SIG_FORCE kernel refuses. That is what happened with the old pinned
# key under a non-default name: do_shared_workdir copies only
# certs/signing_key.*, so v4l2loopback for sargo was packaged unsigned.
#
# Optional: a persistent key kept outside every layer
# ----------------------------------------------------
# Set MODULE_SIG_KEY in local.conf (or a CI secret's environment) to the
# absolute path of one PEM file holding the private key and its X.509
# certificate, for example a file in a private luneos-secrets checkout:
#
#   MODULE_SIG_KEY = "/home/<user>/Documents/GitHub/luneos-secrets/module-signing/<name>.pem"
#
# Generate it with the same settings certs/Makefile uses:
#
#   openssl req -new -nodes -utf8 -sha512 -days 36500 -batch -x509 \
#       -config certs/x509.genkey -outform PEM \
#       -out <name>.pem -keyout <name>.pem
#
# The path is written into CONFIG_MODULE_SIG_KEY as is. kbuild then reads the
# key from that path for the kernel's own modules and, through the same
# .config in STAGING_KERNEL_BUILDDIR, for out-of-tree modules; the private key
# is never copied into ${B}, STAGING_KERNEL_BUILDDIR or sstate. Only the
# extracted public certificate (certs/signing_key.x509) is. The path itself
# does end up in the kernel's .config (IKCONFIG and the kernel-dev package),
# so pick one that does not matter if seen.
#
# Only the path is part of the task signature, not the key's contents. When the
# key is rotated in place, also change the file name (or bump
# MACHINE_KERNEL_PR) so the kernel and every out-of-tree module rebuild.
MODULE_SIG_KEY ??= ""

do_configure:append() {
    sed -i -e '/^CONFIG_MODULE_SIG_KEY=/d' -e '/^# CONFIG_MODULE_SIG_KEY is not set/d' ${B}/.config
    if [ -n "${MODULE_SIG_KEY}" ]; then
        case "${MODULE_SIG_KEY}" in
        /*) ;;
        *) bbfatal "MODULE_SIG_KEY must be an absolute path, got '${MODULE_SIG_KEY}'" ;;
        esac
        if [ ! -r "${MODULE_SIG_KEY}" ]; then
            bbfatal "MODULE_SIG_KEY='${MODULE_SIG_KEY}' does not exist or is not readable"
        fi
        if ! grep -q -e '-----BEGIN .*PRIVATE KEY-----' "${MODULE_SIG_KEY}" || \
           ! grep -q -e '-----BEGIN CERTIFICATE-----' "${MODULE_SIG_KEY}"; then
            bbfatal "MODULE_SIG_KEY='${MODULE_SIG_KEY}' must hold both a private key and its X.509 certificate in PEM form"
        fi
        echo "CONFIG_MODULE_SIG_KEY=\"${MODULE_SIG_KEY}\"" >> ${B}/.config
    else
        echo 'CONFIG_MODULE_SIG_KEY="certs/signing_key.pem"' >> ${B}/.config
    fi
}

# Refuse a private key shipped in this recipe's own SRC_URI. LuneOS runs the
# same check for every recipe (luneos_signing_qa.bbclass in meta-luneos, which
# also fails out-of-tree modules left unsigned); this copy keeps the kernels
# that sign modules covered in a build with another distro.
python kernel_module_signing_check_srcuri() {
    import re
    if bb.data.inherits_class('luneos_signing_qa', d):
        return
    marker = re.compile(rb'-----BEGIN (?:[A-Z0-9]+ )*PRIVATE KEY-----')
    fetcher = bb.fetch2.Fetch((d.getVar('SRC_URI') or '').split(), d)
    found = []
    for url in fetcher.urls:
        if fetcher.ud[url].type != 'file':
            continue
        local = fetcher.localpath(url)
        if not local or not os.path.exists(local):
            continue
        paths = [local]
        if os.path.isdir(local):
            paths = [os.path.join(r, f) for r, _, fs in os.walk(local) for f in fs]
        for p in paths:
            try:
                with open(p, 'rb') as f:
                    if marker.search(f.read()):
                        found.append(p)
            except OSError:
                pass
    if found:
        bb.fatal('%s: SRC_URI ships a private key; keep signing keys out of the '
                 'layer and set MODULE_SIG_KEY instead:\n  %s'
                 % (d.getVar('PN'), '\n  '.join(found)))
}
do_unpack[prefuncs] += "kernel_module_signing_check_srcuri"
