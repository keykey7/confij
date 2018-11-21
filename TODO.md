# IntelliJ Dev Setup
* manually enable annotation processing: 
  in Settings… → Build, Execution, Deployment → Compiler → Annotation Processors, 
  check `Enable annotation processing` and `Obtain processors from project classpath`
* use the Intellij Lombok Plugin

# TODO
- add integration tests for each mapping type
- extend @Base64 with different charsets and base64 standards
- implement @CsvSeparated (for array and lists)
- load from GIT source
- load via jdbc
- load via JNDI
- support Random or @Random for random values
- load resources with wildcard support (like *.properties)
- reloader with file-watcher
- validation module with everything @NotNull and everything @Valid per default
- logging modul
- injection support: guice, spring, J2EE
- @Default with support for primitives
- add priorities to serviceloader
- convert StaticFunctionMapper and alike to normal ValueMapperFactories to allow using them in annotations
- VariableResolvers should always use the VariableResolvers of referenced leaves
