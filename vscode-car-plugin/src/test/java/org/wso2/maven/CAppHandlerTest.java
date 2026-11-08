/*
 * Copyright (c) 2025, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.maven;

import org.apache.axiom.om.OMElement;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wso2.maven.libraries.CAppDependencyResolver;
import org.wso2.maven.model.ArtifactDependency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CAppHandlerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    static class MockCARMojo extends CARMojo {
        private final StringBuilder logs = new StringBuilder();
        @Override public void logInfo(String msg) { logs.append(msg).append("\n"); }
        @Override public void logError(String msg) { logs.append("ERROR: ").append(msg).append("\n"); }
    }

    @Test
    public void testCreateDependencyDescriptorXml_ReturnsCorrectXml() {

        String projectName = "com.example_test_1.0.0";
        CARMojo mojo = new CAppHandlerTest.MockCARMojo();
        CAppHandler handler = new CAppHandler("test", mojo);

        OMElement result = handler.createDependencyDescriptorXml(projectName,false, java.util.Collections.<CAppDependency>emptyList(), false);

        assertNotNull(result);
        assertEquals("project", result.getLocalName());
        OMElement idElement = result.getFirstChildWithName(new javax.xml.namespace.QName("id"));
        assertNotNull(idElement);
        assertEquals(projectName, idElement.getText());
        OMElement dependenciesElement = result.getFirstChildWithName(new javax.xml.namespace.QName("dependencies"));
        assertNotNull(dependenciesElement);
    }

    @Test
    public void testCreateDependencyDescriptorXml_WithDependencies() {
        String projectName = "com.example_test_1.0.0";
        CARMojo mojo = new MockCARMojo();
        CAppHandler handler = new CAppHandler("test", mojo);

        List<CAppDependency> deps = Arrays.asList(new CAppDependency("group1", "artifact1", "1.0.0"),
                new CAppDependency("group2", "artifact2", "2.0.0"));

        OMElement result = handler.createDependencyDescriptorXml(projectName, false, deps, false);

        OMElement dependenciesElement = result.getFirstChildWithName(new javax.xml.namespace.QName("dependencies"));
        assertNotNull(dependenciesElement);

        OMElement depElem1 = dependenciesElement.getFirstElement();
        assertNotNull(depElem1);
        assertEquals("group1", depElem1.getAttributeValue(new javax.xml.namespace.QName("groupId")));
        assertEquals("artifact1", depElem1.getAttributeValue(new javax.xml.namespace.QName("artifactId")));
        assertEquals("1.0.0", depElem1.getAttributeValue(new javax.xml.namespace.QName("version")));
        assertEquals("car", depElem1.getAttributeValue(new javax.xml.namespace.QName("type")));

        OMElement depElem2 = (OMElement) depElem1.getNextOMSibling();
        assertNotNull(depElem2);
        assertEquals("group2", depElem2.getAttributeValue(new javax.xml.namespace.QName("groupId")));
        assertEquals("artifact2", depElem2.getAttributeValue(new javax.xml.namespace.QName("artifactId")));
        assertEquals("2.0.0", depElem2.getAttributeValue(new javax.xml.namespace.QName("version")));
        assertEquals("car", depElem2.getAttributeValue(new javax.xml.namespace.QName("type")));
        assertEquals(2, dependenciesElement.getChildElements().hasNext() ? 2 : 0);
    }

    @Test
    public void testCreateDependencyDescriptorXml_versionedDeployment() {

        String projectName = "com.example_test_1.0.0";
        CARMojo mojo = new CAppHandlerTest.MockCARMojo();
        CAppHandler handler = new CAppHandler("test", mojo);

        OMElement result = handler.createDependencyDescriptorXml(projectName,true, java.util.Collections.<CAppDependency>emptyList(), false);

        assertNotNull(result);
        assertEquals("project", result.getLocalName());
        OMElement idElement = result.getFirstChildWithName(new javax.xml.namespace.QName("id"));
        assertNotNull(idElement);
        assertEquals(projectName, idElement.getText());
        OMElement versionedDeploymentElement = result.getFirstChildWithName(new javax.xml.namespace.QName("versionedDeployment"));
        assertNotNull(idElement);
        assertEquals("true", versionedDeploymentElement.getText());
        OMElement dependenciesElement = result.getFirstChildWithName(new javax.xml.namespace.QName("dependencies"));
        assertNotNull(dependenciesElement);
    }

    @Test
    public void testProcessArtifacts_StreamSequenceIsPackaged() throws Exception {

        File artifactsFolder = tempFolder.newFolder("artifacts");
        File streamSequencesFolder = new File(artifactsFolder, Constants.STREAM_SEQUENCES_DIR_NAME);
        assertTrue(streamSequencesFolder.mkdir());

        File sequenceFile = new File(streamSequencesFolder, "cohort-run.xml");
        try (FileWriter writer = new FileWriter(sequenceFile)) {
            writer.write("<streamSequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"cohort-run\" "
                    + "sourceIdentity=\"strict\">"
                    + "<source><file.streamRead name=\"share\" configKey=\"connSmb\">"
                    + "<path>in/cohort.csv</path></file.streamRead></source>"
                    + "<sink><file.streamWrite name=\"out\" configKey=\"connSmb\">"
                    + "<path>out/cohort.csv</path></file.streamWrite></sink>"
                    + "</streamSequence>");
        }

        File archiveFolder = tempFolder.newFolder("archive");
        CARMojo mojo = new MockCARMojo();
        CAppHandler handler = new CAppHandler("test", mojo);

        List<ArtifactDependency> dependencies = new ArrayList<>();
        handler.processArtifacts(artifactsFolder, archiveFolder.getAbsolutePath(), dependencies,
                new ArrayList<ArtifactDependency>(), "1.0.0");

        assertEquals(1, dependencies.size());
        ArtifactDependency dependency = dependencies.get(0);
        assertEquals("cohort-run", dependency.getArtifact());
        assertEquals("1.0.0", dependency.getVersion());
        assertEquals(Constants.SERVER_ROLE_EI, dependency.getServerRole());
        assertTrue(dependency.getInclude());

        File artifactDir = new File(archiveFolder, "cohort-run_1.0.0");
        File artifactXml = new File(artifactDir, Constants.ARTIFACT_XML);
        assertTrue(artifactXml.exists());

        String artifactXmlContent = new String(Files.readAllBytes(artifactXml.toPath()), StandardCharsets.UTF_8);
        assertTrue(artifactXmlContent.contains("type=\"" + Constants.STREAM_SEQUENCE_TYPE + "\""));
        assertTrue(artifactXmlContent.contains("name=\"cohort-run\""));
        assertTrue(artifactXmlContent.contains("<file>cohort-run-1.0.0.xml</file>"));

        assertTrue(new File(artifactDir, "cohort-run-1.0.0.xml").exists());
    }
}
