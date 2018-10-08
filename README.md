# TODO
- add integration tests for each mapping type
- extend @Base64 with different charsets and base64 standards
- implement @CsvSeparated (for array and lists)
- a practical toString(), equal()... for interfaces
- load from GIT source
- load via jdbc
- load via JNDI
- support Random or @Random for random values
- load resources with wildcard support (like *.properties)
- configuration reload support: new interface Reloadable? or a Reloader.reload(cfg) ?
- ...with locking functionality
    lock types: async (never blocking) vs blocking
    trigger type: time based, file listener, manual
- validation module with everything @NotNull and everything @Valid per default
- logging modul
- refactor: remove Lists, replace with maps + key validator (aka only integers, sequential)
- documentation
- injection support: guice, spring, J2EE
