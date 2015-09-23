/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.maven.analytics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.wso2.maven.capp.model.Artifact;
import org.wso2.maven.capp.mojo.AbstractPOMGenMojo;
import org.wso2.maven.capp.utils.CAppMavenUtils;
import org.wso2.maven.capp.utils.WSO2MavenPluginConstantants;
import org.wso2.maven.analytics.utils.AnalyticsMavenUtils;;

/**
 * This is the Maven Mojo used for generating a pom for a dataservice artifact
 * from the old CApp project structure
 * 
 * @goal pom-gen
 * 
 */
public abstract class AnalyticsPOMGenMojo extends AbstractPOMGenMojo {

	/**
	 * @parameter default-value="${project}"
	 */
	public MavenProject project;

	/**
	 * Maven ProjectHelper.
	 * 
	 * @component
	 */
	public MavenProjectHelper projectHelper;

	/**
	 * The path of the location to output the pom
	 * 
	 * @parameter expression="${project.build.directory}/artifacts"
	 */
	public File outputLocation;

	/**
	 * The resulting extension of the file
	 * 
	 * @parameter
	 */
	public File artifactLocation;

	/**
	 * POM location for the module project
	 * 
	 * @parameter expression="${project.build.directory}/pom.xml"
	 */
	public File moduleProject;

	/**
	 * Group id to use for the generated pom
	 * 
	 * @parameter
	 */
	public String groupId;

	/**
	 * Comma separated list of "artifact_type=extension" to be used when
	 * creating dependencies for other capp artifacts
	 * 
	 * @parameter
	 */
	public String typeList;

	private List<AnalyticsArtifact> retrieveArtifacts() {
		return AnalyticsMavenUtils.retrieveArtifacts(getArtifactLocation());
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		List<AnalyticsArtifact> artifacts = retrieveArtifacts();

		// Artifact list
		List<Artifact> mappedArtifacts = new ArrayList<Artifact>();

		for (AnalyticsArtifact analyticsArtifact : artifacts) {
			Artifact artifact = new Artifact();
			artifact.setName(analyticsArtifact.getName());
			artifact.setVersion(analyticsArtifact.getVersion());
			artifact.setType(analyticsArtifact.getType());
			artifact.setServerRole(analyticsArtifact.getServerRole());
			artifact.setFile(analyticsArtifact.getFile());
			artifact.setSource(new File(getArtifactLocation(), "artifact.xml"));
			mappedArtifacts.add(artifact);
		}
		super.processArtifacts(mappedArtifacts);

	}

	protected void copyResources(MavenProject project, File projectLocation, Artifact artifact) throws IOException {
		File analyticsArtifact = processTokenReplacement(artifact);
		if (analyticsArtifact == null) {
			analyticsArtifact = artifact.getFile();
		}
		FileUtils.copyFile(analyticsArtifact, new File(projectLocation, artifact.getFile().getName()));
	}

	protected void addPlugins(MavenProject artifactMavenProject,
			Artifact artifact) {
		Plugin plugin = CAppMavenUtils.createPluginEntry(artifactMavenProject,
				"org.wso2.maven", pluginName(),
				WSO2MavenPluginConstantants.MAVEN_DATA_SERVICE_PLUGIN_VERSION,
				true);
		Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
		// add configuration
		Xpp3Dom aritfact = CAppMavenUtils.createConfigurationNode(
				configuration, "artifact");
		aritfact.setValue(artifact.getFile().getName());
	}
	
	protected String pluginName(){
		return null;
	}

}
