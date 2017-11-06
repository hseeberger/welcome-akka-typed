// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `welcome-akka-typed` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin, GitVersioning)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaTyped,
        library.akkaTypedTestkit % Test,
        library.scalaCheck       % Test,
        library.scalaTest        % Test
      )
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akka       = "2.5.8"
      val scalaCheck = "1.13.5"
      val scalaTest  = "3.0.4"
    }
    val akkaTyped        = "com.typesafe.akka" %% "akka-typed"         % Version.akka
    val akkaTypedTestkit = "com.typesafe.akka" %% "akka-typed-testkit" % Version.akka
    val scalaCheck       = "org.scalacheck"    %% "scalacheck"         % Version.scalaCheck
    val scalaTest        = "org.scalatest"     %% "scalatest"          % Version.scalaTest
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  gitSettings ++
  scalafmtSettings

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    // scalaVersion := "2.12.4",
    organization := "de.heikoseeberger",
    organizationName := "Heiko Seeberger",
    startYear := Some(2017),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8"
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value)
)

lazy val gitSettings =
  Seq(
    git.useGitDescribe := true
  )

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )
