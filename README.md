Maven 2 Uberjar Plugin
----------------------

This is a Maven 2 Uberjar Plugin which creates uberjars using the [Classworlds Uberjarring](http://classworlds.codehaus.org/uberjar.html) technic. It builds the uberjar with the [Shrinkwrap Uberjar](https://github.com/cedricvidal/shrinkwrap-uberjar) library.

Content of the built uberjar

	+-- uberjar.jar
		+-- META-INF
		¦   +-- MANIFEST.MF
		+-- WORLDS-INF
		¦   +-- classworlds.jar
		¦   +-- conf
		¦   ¦   +-- classworlds.conf
		¦   +-- lib
		¦       +-- my-app.jar
		¦       +-- some-dependency.jar
		¦       +-- another-dependency.jar
		+-- org
			+-- codehaus
				+-- classworlds
					+-- UrlUtils.class
					+-- uberjar
						+-- boot
						¦   +-- Bootstrapper.class
						¦   +-- InitialClassLoader.class
						+-- protocol
							+-- jar
								+-- Handler.class
								+-- JarUrlConnection.class

[![Build Status](https://cedricvidal.ci.cloudbees.com/job/maven-uberjar-plugin/badge/icon)](https://cedricvidal.ci.cloudbees.com/job/maven-uberjar-plugin/)

# Usage

#### Simply add the plugin to your project with the default configuration

This configuration will create an uberjar that when executed using java -jar will run the com.foo.bar.SampleApp:

	<plugin>
		<groupId>biz.vidal.maven</groupId>
		<artifactId>maven-uberjar-plugin</artifactId>
		<version>0.0.1</version>
		<configuration>
			<mainClass>com.foo.bar.SampleApp</mainClass>
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

#### Don't forget to also declare the Maven repository the plugin is deployed in:

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

# Attaching the uberjar using a classifier

By default, the uberjar will be the default artifact of the maven project but if you specify a classifier, then your uberjar will instead be attached to the project:

	<configuration>
		<classifier>uber</classifier>
	</configuration>

# Using different classworld and classworlds-boot artifacts

By default, your uberjar will be built to run using classworlds:classworlds:1.1 and classworlds:classworlds-boot:1.0 jars but if you want your uberjar to run using a different classworlds version:

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

[![Build on Cloudbees](http://web-static-cloudfront.s3.amazonaws.com/images/badges/BuiltOnDEV.png)](http://www.cloudbees.com/)
