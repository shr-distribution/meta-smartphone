# Fail the image build if the rootfs stops being device-agnostic.
#
# The claim this protects: one LuneOS arm64 rootfs boots any Halium device, with
# per-device cost reduced to a boot image and a few config files. Nothing in the
# build enforces that, and the failure mode is silent - a package quietly built
# for one device still produces a working image *on that device*, so the
# regression is only noticed on the next port, long after the cause.
#
# What is checked: every package installed into the image must come from an arch
# that is not tied to a single device. On halium-arm64 that means the shared
# TUNE_PKGARCH (aarch64-halium), noarch, and the generic machine's own
# MACHINE_ARCH. A package from sargo/, tissot_halium/ or any other device feed
# is the bug.
#
# Note this deliberately does NOT ban MACHINE_ARCH outright, which is what §6.2
# of the migration plan originally proposed. halium-arm64 is itself the generic
# machine, so its MACHINE_ARCH is generic; some 45 packages legitimately land
# there (base-files, opkg-arch-config, systemd-conf, keymaps and the Halium glue).
# Banning it would fail every build for no benefit. The arch a package is built
# *for* is the signal; the arch mechanism is not.
GENERIC_ROOTFS_ALLOWED_ARCHS ?= "all any noarch ${TUNE_PKGARCH} ${MACHINE_ARCH}"

python generic_rootfs_qa() {
    # IMAGE_MANIFEST points into IMGDEPLOYDIR, which do_image_complete has
    # already drained by the time do_image_qa runs, so fall back to the copy in
    # the deploy directory.
    candidates = [d.getVar('IMAGE_MANIFEST')]
    deploy = d.getVar('DEPLOY_DIR_IMAGE')
    link = d.getVar('IMAGE_LINK_NAME')
    if deploy and link:
        candidates.append(os.path.join(deploy, link + '.manifest'))
    manifest = next((c for c in candidates if c and os.path.exists(c)), None)

    # Fatal, not a warning. A check that quietly passes when it cannot find its
    # input is worse than no check: it reports success forever while testing
    # nothing, which is the exact failure this class exists to catch.
    if not manifest:
        bb.fatal('generic-rootfs-qa: no image manifest found, so genericity '
                 'could not be checked. Looked at:\n  %s'
                 % '\n  '.join(c for c in candidates if c))

    allowed = set((d.getVar('GENERIC_ROOTFS_ALLOWED_ARCHS') or '').split())
    offenders = {}
    with open(manifest, 'r') as f:
        for line in f:
            parts = line.split()
            if len(parts) < 2:
                continue
            pkg, arch = parts[0], parts[1]
            if arch not in allowed:
                offenders.setdefault(arch, []).append(pkg)

    if not offenders:
        bb.note('generic-rootfs-qa: rootfs is device-agnostic (arches: %s)'
                % ' '.join(sorted(allowed)))
        return

    lines = ['%s is not device-agnostic: it installs packages built for a '
             'device-specific arch.' % d.getVar('IMAGE_BASENAME')]
    for arch in sorted(offenders):
        pkgs = sorted(offenders[arch])
        shown = ', '.join(pkgs[:12]) + (' ...' if len(pkgs) > 12 else '')
        lines.append('  %s (%d): %s' % (arch, len(pkgs), shown))
    lines.append('')
    lines.append('Allowed arches: %s' % ' '.join(sorted(allowed)))
    lines.append('Move the offending content to the adaptation (a boot image or '
                 'a config file derived at runtime), or make the recipe generic. '
                 'If an arch is genuinely device-agnostic, add it to '
                 'GENERIC_ROOTFS_ALLOWED_ARCHS with a comment saying why.')
    bb.fatal('\n'.join(lines))
}

IMAGE_QA_COMMANDS += "generic_rootfs_qa"
