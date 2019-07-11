Privacy Plugin for TOS
====

This project implements a plugin for the Pentaho Data Integration platform which provides methods for assessing and managing re-identification risks based on the methodology proposed in "El Emam, Khaled, Guide to the De-Identification of Personal Health Information, CRC Press, 2013".

System-Requirements
------
* Talend Open Studio Version 7.2.1 M3 or higher.
* For Maven is Version 3.5 or higher required.
* Java Version 1.8 or higher

Compilation
------
As a prerequisite, libarx-3.7.1 have to be deployed to the local maven repository. To this end, execute the following commands:

```bash
mvnw install:install-file -Dfile=lib/arx/libarx-3.7.1.jar -DgroupId=org.deidentifier.arx -DartifactId=libarx -Dversion=3.7.1 -Dpackaging=jar
```

For the actual compilation, build and deploy, execute, the following commands:
```bash
cd ${project.dir}
```

```bash
mvnw clean install
mvnw talend-component:deploy-in-studio -Dtalend.component.studioHome=<Studio-Home>
```

License
------

GPLv3