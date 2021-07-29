do_install:append() {
    # Add necessary environment settings to execute android binaries
    echo "LD_LIBRARY_PATH=\$LD_LIBRARY_PATH:/system/lib:/system/lib64" >> ${D}${sysconfdir}/profile
    echo "PATH=\$PATH:/system/bin" >> ${D}${sysconfdir}/profile
    echo "export LD_LIBRARY_PATH PATH" >> ${D}${sysconfdir}/profile
}
