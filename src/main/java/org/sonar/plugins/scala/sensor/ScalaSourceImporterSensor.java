/*
 * Sonar Scala Plugin
 * Copyright (C) 2011 - 2014 All contributors
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.scala.sensor;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Phase.Name;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.scala.language.Scala;
import org.sonar.plugins.scala.language.ScalaFile;

import java.io.IOException;

/**
 * This Sensor imports all Scala files into Sonar.
 *
 * @author Felix Müller
 * @since 0.1
 */
@Phase(name = Name.PRE)
public class ScalaSourceImporterSensor extends AbstractScalaSensor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScalaSourceImporterSensor.class);

  public ScalaSourceImporterSensor(FileSystem fileSystem, Scala scala) {
    super(fileSystem, scala);
  }

  public void analyse(Project project, SensorContext sensorContext) {
    String charset = fileSystem.encoding().toString();
    FilePredicates p = fileSystem.predicates();
    for (InputFile sourceFile : fileSystem.inputFiles(p.and(p.hasLanguage(Scala.INSTANCE.getKey()), p.hasType(InputFile.Type.MAIN)))) {
      addFileToSonar(sensorContext, sourceFile, false, charset);
    }

    for (InputFile testFile : fileSystem.inputFiles(p.and(p.hasLanguage(Scala.INSTANCE.getKey()), p.hasType(InputFile.Type.TEST)))) {
      addFileToSonar(sensorContext, testFile, true, charset);
    }
  }

  private void addFileToSonar(SensorContext sensorContext, InputFile inputFile, boolean isUnitTest, String charset) {
    try {
      String source = FileUtils.readFileToString(inputFile.file(), charset);
      ScalaFile file = ScalaFile.fromInputFile(inputFile, isUnitTest);

      sensorContext.index(file);
      sensorContext.saveSource(file, source);

      if (LOGGER.isDebugEnabled()) {
        if (isUnitTest) {
          LOGGER.debug("Added Scala test file to Sonar: {}", file);
        } else {
          LOGGER.debug("Added Scala source file to Sonar: {}", file);
        }
      }
    } catch (IOException ioe) {
      LOGGER.error("Could not read the file: " + inputFile.file().getAbsolutePath(), ioe);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
