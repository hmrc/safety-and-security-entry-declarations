import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.24.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % "0.63.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.24.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % "0.63.0",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8",
    "org.scalatest"           %% "scalatest"                  % "3.2.10",
    "org.scalatestplus"       %% "scalacheck-1-15"            % "3.2.10.0",
    "org.scalatestplus"       %% "mockito-3-4"                % "3.2.10.0",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0",
    "org.mockito"             %% "mockito-scala"              % "1.16.42",
    "org.scalacheck"          %% "scalacheck"                 % "1.15.4",
    "com.vladsch.flexmark"    % "flexmark-all"                % "0.62.2"
  ).map(_ % "test, it")
}
