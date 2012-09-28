package org.unidal.maven.plugin.codegen;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.unidal.codegen.generator.GenerateContext;
import org.unidal.codegen.generator.GenerateContextSupport;
import org.unidal.codegen.generator.Generator;

import com.site.helper.Splitters;

/**
 * DAL code generator for iBatis.
 * 
 * @goal dal-ibatis
 * @author Frankie Wu
 */
public class DalIbatisMojo extends AbstractMojo {

   /**
    * XSL code generator implementation
    * 
    * @component role="org.unidal.codegen.generator.Generator"
    *            role-hint="dal-ibatis"
    * @required
    * @readonly
    */
   protected Generator m_generator;

   /**
    * Current project
    * 
    * @parameter expression="${project}"
    * @required
    * @readonly
    */
   protected MavenProject m_project;

   /**
    * Current project base directory
    * 
    * @parameter expression="${sourceDir}"
    *            default-value="${basedir}/target/generated-sources/dal-ibatis/src/main/java"
    * @required
    */
   protected String sourceDir;
   
   /**
    * Current project base directory
    * 
    * @parameter expression="${resourceDir}"
    *            default-value="${basedir}/target/generated-sources/dal-ibatis/src/main/resources"
    * @required
    */
   protected String resourceDir;

   /**
    * Location of manifest.xml file
    * 
    * @parameter expression="${manifest}" default-value=
    *            "${basedir}/src/main/resources/META-INF/dal/jdbc/manifest.xml"
    * @required
    */
   protected String manifest;

   /**
    * Location of XSL template base.
    * 
    * @parameter expression="${resource.base}"
    *            default-value="/META-INF/dal/ibatis"
    * @required
    */
   protected String resouceBase;

   /**
    * Verbose information or not
    * 
    * @parameter expression="${verbose}" default-value="false"
    */
   protected boolean verbose;

   /**
    * Verbose information or not
    * 
    * @parameter expression="${debug}" default-value="false"
    */
   protected boolean debug;

   /**
    * Skip this codegen or not
    * 
    * @parameter expression="${codegen.skip}" default-value="false"
    */
   protected boolean skip;

   public void execute() throws MojoExecutionException, MojoFailureException {
      if (skip) {
         getLog().info("Model codegen was skipped explicitly.");
         return;
      }

      try {
         List<String> parts = Splitters.by(',').noEmptyItem().trim().split(manifest);

         for (String part : parts) {
            generateJdbc(part);
         }
      } catch (Exception e) {
         throw new MojoExecutionException("Code generating failed.", e);
      }
   }

   private void generateJdbc(String part) throws MojoExecutionException, IOException, MalformedURLException, Exception {
      File manifestFile = new File(part);

      if (!manifestFile.exists()) {
         throw new MojoExecutionException(String.format("Manifest(%s) not found!", manifestFile.getCanonicalPath()));
      }

      final URL manifestXml = manifestFile.toURI().toURL();
      final GenerateContext ctx = new GenerateContextSupport(resouceBase, m_project.getBasedir()) {
         @Override
         protected void configure(Map<String, String> properties) {
            properties.put("src-main-java", sourceDir);
            properties.put("src-main-resources", resourceDir);
         }

         public URL getManifestXml() {
            return manifestXml;
         }

         public void log(LogLevel logLevel, String message) {
            switch (logLevel) {
            case DEBUG:
               if (debug) {
                  getLog().debug(message);
               }
               break;
            case INFO:
               if (debug || verbose) {
                  getLog().info(message);
               }
               break;
            case ERROR:
               getLog().error(message);
               break;
            }
         }
      };

      m_generator.generate(ctx);
      m_project.addCompileSourceRoot(sourceDir);
      getLog().info(ctx.getGeneratedFiles() + " files generated.");
   }
}
