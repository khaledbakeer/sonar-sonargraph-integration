@rem Sample commandline to execute Sonargraph and SonarQube for the Sonargraph-SonarQube plugin.  
mvn clean verify com.hello2morrow:sonargraph-maven-plugin:9.13.0:create-report -Dsonargraph.prepareForSonarQube=true -Dsonargraph.autoUpdate=false -Dsonargraph.installationDirectory=D:\00_repo\sgng\products\SonargraphBuild\dist\release\SonargraphBuild -Dsonargraph.licenseFile=C:\Users\Ingmar\AppData\Roaming\hello2morrow\Sonargraph\Standalone\Sonargraph.license sonar:sonar 