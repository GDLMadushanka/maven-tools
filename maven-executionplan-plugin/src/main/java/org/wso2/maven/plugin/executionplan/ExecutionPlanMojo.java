/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.maven.plugin.executionplan;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.wso2.maven.analytics.AnalyticsMojo;


/**
 * This is the Maven Mojo used for Analytics resources to be copied to the output directory in the resource-process
 * phase.
 * 
 * @goal package-analytics
 * 
 */
public class ExecutionPlanMojo extends AnalyticsMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();
	}	

}
