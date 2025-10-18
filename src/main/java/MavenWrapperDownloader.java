/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class MavenWrapperDownloader {

    private static final String WRAPPER_VERSION = "3.2.0";
    /**
     * Default URL to download the maven-wrapper.jar from, if no 'downloadUrl' is provided.
     */
    private static final String DEFAULT_DOWNLOAD_URL = "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/"
            + WRAPPER_VERSION + "/maven-wrapper-" + WRAPPER_VERSION + ".jar";

    /**
     * Path to the maven-wrapper.properties file, which might contain a downloadUrl property to
     * use instead of the default one.
     */
    private static final String MAVEN_WRAPPER_PROPERTIES_PATH =
            ".mvn/wrapper/maven-wrapper.properties";

    /**
     * Path where the maven-wrapper.jar will be saved to.
     */
    private static final String MAVEN_WRAPPER_JAR_PATH =
            ".mvn/wrapper/maven-wrapper.jar";

    /**
     * Name of the property which should be used to override the default download url for the wrapper.
     */
    private static final String PROPERTY_NAME_WRAPPER_URL = "wrapperUrl";

    public static void main(String[] args) {
        System.out.println("- Downloader started");
        Path baseDirectory = Paths.get(args[0]);
        try {
            System.out.println("- Using base directory: " + baseDirectory.toAbsolutePath());

            // If the maven-wrapper.properties exists, read it and check if it contains a custom
            // wrapperUrl parameter.
            Path mavenWrapperPropertyFile = baseDirectory.resolve(MAVEN_WRAPPER_PROPERTIES_PATH);
            String url = DEFAULT_DOWNLOAD_URL;
            if (Files.exists(mavenWrapperPropertyFile)) {
                try (InputStream in = Files.newInputStream(mavenWrapperPropertyFile)) {
                    java.util.Properties p = new java.util.Properties();
                    p.load(in);
                    url = p.getProperty(PROPERTY_NAME_WRAPPER_URL, url);
                }
            }
            System.out.println("- Downloading from: " + url);

            Path outputFile = baseDirectory.resolve(MAVEN_WRAPPER_JAR_PATH);
            // If a download is required, then create the authenticator, as the download may be from a protected repo
            if (url.startsWith("http") && System.getenv("MVNW_USERNAME") != null && System.getenv("MVNW_PASSWORD") != null) {
                String username = System.getenv("MVNW_USERNAME");
                char[] password = System.getenv("MVNW_PASSWORD").toCharArray();
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
            }
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, outputFile, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
