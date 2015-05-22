
organization := "com.olx"

name := "location-scala"

version := scala.util.Properties.envOrElse("VERSION_BUILD_NUMBER", "1")

libraryDependencies ++= Seq(
	ws,
	"org.mongodb" %% "casbah" % "2.7.5",
	"com.novus" %% "salat" % "1.9.9",
	"olx" % "godzuki" % "1.3",
	"com.olx.innovations" % "framework" % "0.1.11",
	"org.mockito" % "mockito-all" % "1.9.0",
	"com.wordnik" %% "swagger-play2" % "1.3.12",
	"com.wordnik" %% "swagger-play2-utils" % "1.3.12",
	"net.logstash.logback" % "logstash-logback-encoder" % "3.0"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(
	credentials += Credentials("Artifactory Realm", "artifactory.innovations.olx.com", "admin", "PypW2Hkm0SP5"),
	resolvers ++= Seq("Artifactory Snapshot" at "http://artifactory.innovations.olx.com:8081/artifactory/libs-snapshot",
	"Artifactory Release" at "http://artifactory.innovations.olx.com:8081/artifactory/libs-release",
	"Apache Snapshot" at "https://repository.apache.org/snapshots/",
	"Lucene Snapshot" at "https://download.elasticsearch.org/lucenesnapshots/1652032"),
	publishArtifact in Test := false,
	publishArtifact in (Compile, packageDoc) := false,
	publishArtifact in (Compile, packageSrc) := false,
	publishTo := Some("Artifactory Realm" at "http://artifactory.innovations.olx.com:8081/artifactory/location")
)

scalaVersion := "2.11.6"

lazy val dist = com.typesafe.sbt.SbtNativePackager.NativePackagerKeys.dist

publish <<= (publish) dependsOn dist

publishLocal <<= (publishLocal) dependsOn dist

val distHack = TaskKey[File]("dist-hack", "Hack to publish dist")

artifact in distHack ~= { (art: Artifact) => art.copy(`type` = "zip",extension
= "zip") }


val distHackSettings = Seq[Setting[_]] (
  distHack <<= (target in Universal, normalizedName, version) map { (
targetDir, id, version) =>
       val packageName = "%s-%s" format(id, version)
       targetDir / (packageName + ".zip")
     }) ++ Seq(addArtifact(artifact in distHack, distHack).settings: _*)

seq(distHackSettings: _*)


