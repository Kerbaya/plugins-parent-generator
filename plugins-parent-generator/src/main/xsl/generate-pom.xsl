<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!--
Uses the XML output from 
org.codehaus.mojo:versions-maven-plugin:plugin-updates-report to produce an 
updated pom.xml for plugins-parent  
-->

<xsl:param name="previousVersion"/>
<xsl:param name="timestamp"/>
<xsl:param name="generatorGav"/>
<xsl:param name="parentGav"/>

<xsl:output omit-xml-declaration="yes"/>

<xsl:template match="/">

<xsl:text disable-output-escaping="yes"><![CDATA[<project xmlns="http://maven.apache.org/POM/4.0.0"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd"
		child.project.url.inherit.append.path="false">
]]></xsl:text>

<xsl:text disable-output-escaping="yes">&#10;&lt;!--</xsl:text>

<xsl:text disable-output-escaping="yes">&#10;Built using: </xsl:text>
<xsl:value-of select="$generatorGav" disable-output-escaping="yes"/>

<xsl:text disable-output-escaping="yes">&#10;Parent: </xsl:text>
<xsl:value-of select="$parentGav" disable-output-escaping="yes"/>

<xsl:text disable-output-escaping="yes">&#10;Build timestamp: </xsl:text>
<xsl:value-of select="$timestamp" disable-output-escaping="yes"/>

<xsl:text disable-output-escaping="yes">&#10;--&gt;&#10;</xsl:text> 

<xsl:text disable-output-escaping="yes"><![CDATA[
<modelVersion>4.0.0</modelVersion>

<groupId>com.kerbaya</groupId>
<artifactId>plugins-parent</artifactId>
]]></xsl:text>

<version>
	<xsl:value-of select="$previousVersion + 1"/>
</version>
<xsl:text disable-output-escaping="yes">&#10;</xsl:text>

<xsl:text disable-output-escaping="yes"><![CDATA[
<packaging>pom</packaging>

<name>Kerbaya Plugins Parent</name>
<description>Parent to define Maven plugins for Kerbaya artifacts</description>

<url>https://github.com/Kerbaya/${project.artifactId}</url>

<licenses>
	<license>
		<name>GNU General Public License v3.0 or later</name>
		<url>https://www.gnu.org/licenses/gpl-3.0-standalone.html</url>
	</license>
</licenses>

<scm child.scm.url.inherit.append.path="false">
	<url>https://github.com/Kerbaya/${project.artifactId}/tree/${project.version}</url>
</scm>

<developers>
	<developer>
		<organization>Kerbaya Software</organization>
		<organizationUrl>https://www.kerbaya.com</organizationUrl>
	</developer>
</developers>

<build>
	<pluginManagement>
		<plugins>
]]></xsl:text>



<xsl:for-each select="/PluginUpdatesReport/pluginManagements/pluginManagement">
	<xsl:text disable-output-escaping="yes">&#9;&#9;&#9;</xsl:text>
	<plugin>
		<xsl:text disable-output-escaping="yes">&#10;</xsl:text>
		
		<xsl:text disable-output-escaping="yes">&#9;&#9;&#9;&#9;</xsl:text>
		<xsl:copy-of select="groupId"/>
		<xsl:text disable-output-escaping="yes">&#10;</xsl:text>
		
		<xsl:text disable-output-escaping="yes">&#9;&#9;&#9;&#9;</xsl:text>
		<xsl:copy-of select="artifactId"/>
		<xsl:text disable-output-escaping="yes">&#10;</xsl:text>

		<xsl:text disable-output-escaping="yes">&#9;&#9;&#9;&#9;</xsl:text>
		<version>
			<xsl:choose>
				<xsl:when test="majors/major">
					<xsl:copy-of select="majors/major[last()]/text()"/>
				</xsl:when>
				<xsl:when test="minors/minor">
					<xsl:copy-of select="minors/minor[last()]/text()"/>
				</xsl:when>
				<xsl:when test="incrementals/incremental">
					<xsl:copy-of select="incrementals/incremental[last()]/text()"/>
				</xsl:when>
				<xsl:when test="nextVersion">
					<xsl:copy-of select="nextVersion/text()"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="currentVersion/text()"/>
				</xsl:otherwise>
			</xsl:choose>
		</version>
		<xsl:text disable-output-escaping="yes">&#10;</xsl:text>
		<xsl:text disable-output-escaping="yes">&#9;&#9;&#9;</xsl:text>
	</plugin>
	<xsl:text disable-output-escaping="yes">&#10;</xsl:text>
</xsl:for-each>



<xsl:text disable-output-escaping="yes"><![CDATA[		</plugins>
	</pluginManagement>
	<plugins>
		<plugin>
			<groupId>org.simplify4u.plugins</groupId>
			<artifactId>sign-maven-plugin</artifactId>
			<inherited>false</inherited>
			<executions>
				<execution>
					<goals>
						<goal>sign</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>

</project>
]]></xsl:text>

</xsl:template>

</xsl:stylesheet>