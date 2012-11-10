Maven 2 Uberjar Plugin
----------------------

This is a Maven 2 Uberjar Plugin which creates uberjars using the [Classworlds Uberjarring](http://classworlds.codehaus.org/uberjar.html) technic. It builds the uberjar with the [Shrinkwrap Uberjar](https://github.com/cedricvidal/shrinkwrap-uberjar) library.

[![Build Status](https://cedricvidal.ci.cloudbees.com/job/maven-uberjar-plugin/badge/icon)](https://cedricvidal.ci.cloudbees.com/job/maven-uberjar-plugin/)

# Usage

#### Add the following Maven repository to your pom.xml

	<repository>
		<id>cedricvidal-cloudbees-release</id>
		<name>cedricvidal-cloudbees-release</name>
		<url>https://repository-cedricvidal.forge.cloudbees.com/release/</url>
		<releases>
			<enabled>true</enabled>
		</releases>
		<snapshots>
			<enabled>false</enabled>
		</snapshots>
	</repository>

#### Add the plugin to your project

	<plugin>
		<groupId>biz.vidal.maven</groupId>
		<artifactId>maven-uberjar-plugin</artifactId>
		<version>0.0.1</version>
		<configuration>
			<mainClass>test.SampleApp</mainClass>
		</configuration>
		<executions>
			<execution>
				<phase>package</phase>
				<goals>
					<goal>uberjar</goal>
				</goals>
			</execution>
		</executions>
	</plugin>

# Using different classworld and classworlds-boot artifacts

If you want your uberjar to run using a different classworlds version:

	<configuration>
		<classworldsArtifact>
			<groupId>classworlds</groupId>
			<artifactId>classworlds</artifactId>
			<version>1.1</version>
		</classworldsArtifact>
		<classworldsBootArtifact>
			<groupId>classworlds</groupId>
			<artifactId>classworlds-boot</artifactId>
			<version>1.0</version>
		</classworldsBootArtifact>
	</configuration>

# Attaching the uberjar using a classifier

	<configuration>
		<classifier>uber</classifier>
	</configuration>

# Building from sources

	$ git clone git://github.com/cedricvidal/maven-uberjar-plugin.git
	$ cd maven-uberjar-plugin
	$ cd sources
	$ mvn install

# About uberjarring

The author of the original maven 1 uberjar plugin wrote an excellent explanation of the uberjarring process:
http://classworlds.codehaus.org/uberjar.html

Cedric Vidal
http://blog.proxiad.com

[![Build on Cloudbees](http://web-static-cloudfront.s3.amazonaws.com/images/badges/BuiltOnDEV.png)](https://cedricvidal.ci.cloudbees.com/)
