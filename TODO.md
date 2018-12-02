# IntelliJ Dev Setup
* manually enable annotation processing: 
  in Settings… → Build, Execution, Deployment → Compiler → Annotation Processors, 
  check `Enable annotation processing` and `Obtain processors from project classpath`
* use the IntelliJ Lombok Plugin

# TODO
- load from GIT source
- load via jdbc
- load via JNDI
- support Random or @Random for random values
- load resources with wildcard support (like *.properties)
- reloader with file-watcher
- validation module with everything @NotNull and everything @Valid per default
- logging modul
- injection support: guice, spring, J2EE
- VariableResolvers should always use the VariableResolvers of referenced leaves
