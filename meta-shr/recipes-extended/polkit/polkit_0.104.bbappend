PRINC := "${@int(PRINC) + 3}"
PACKAGECONFIG = "${@base_contains('DISTRO_FEATURES', 'pam', 'pam', '', d)} ${@base_contains('VIRTUAL-RUNTIME_init_manager', 'sysvinit', 'consolekit', 'systemd', d)}"
