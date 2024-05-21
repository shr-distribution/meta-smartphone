project('sns-reg', 'c')

qrtr_dep = dependency('qrtr', version : '>=1.1')

executable('sns-reg', ['main.c', 'qmi/sns_reg.c', 'map.c'], dependencies : qrtr_dep, install : true)
executable('sns-reg-validator', ['validator.c', 'map.c'], dependencies : qrtr_dep, install : true)
executable('sns-reg-generator', ['generator.c', 'map.c'], dependencies : qrtr_dep, install : true)

systemd_system_unit_dir = get_option('systemd_system_unit_dir')

configure_file(
    copy: true,
    input: 'sns-reg.service',
    install: true,
    install_dir: systemd_system_unit_dir,
    output: 'sns-reg.service',
)
