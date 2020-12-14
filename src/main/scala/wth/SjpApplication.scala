package wth

import com.typesafe.config.ConfigFactory
import org.http4s.client.Client
import org.jsoup.nodes.Document
import wth.http.desser._
import wth.model.Configuration
import wth.model.http.CirceEntitySerDes
import wth.service._
import wth.service.files.FileBasedPhrasesProvider
import wth.service.http._
import zio._
import zio.config._
import zio.config.magnolia.DeriveConfigDescriptor
import zio.config.syntax._
import zio.config.typesafe.TypesafeConfig

object SjpApplication extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    for {
      client <- Http4s.makeManagedHttpClient
      _ <- makeProgram(client)
    } yield ()
  }.exitCode

  def makeProgram(client: TaskManaged[Client[Task]]) =
    Application
      .program[Document]
      .provideSomeLayer[ZEnv](
        resolveProgram(client)
      )

  private def resolveProgram(http4sClient: TaskManaged[Client[Task]]) = {
    val config = TypesafeConfig.fromTypesafeConfig(
      ConfigFactory.load(),
      DeriveConfigDescriptor.descriptor[Configuration]
    )
    val http4sLayer = http4sClient.toLayer.orDie
    val phrasesProvider = wordsProviderLayer(config)
    val repositoryLayer = createRepositoryLayer(config, http4sLayer)
    val queryLayer = createQueryLayer()
    val parserLayer = createParserLayer()

    phrasesProvider ++ queryLayer ++ parserLayer ++ repositoryLayer
  }

  private def wordsProviderLayer(config: Layer[ReadError[String], ZConfig[Configuration]]) = {
    val wordsPath = config.narrow(_.words_path)
    wordsPath >>> FileBasedPhrasesProvider.service
  }

  private def createParserLayer() =
    SjpHtmlResponseParser.jsoupSjpHtmlParser

  private def createQueryLayer() =
    SjpPwnQuery.jsoupService

  private def createRepositoryLayer(config: Layer[ReadError[String], ZConfig[Configuration]],
                                    http4sLayer: ULayer[Has[Client[Task]]]
  ) = {
    val httpsLayer = http4sLayer >>> Http4s.http4s
    val ankiCfg = config.narrow(_.anki)

    (addNoteSerDes ++ createDeckSerDer ++ ankiCfg ++ httpsLayer) >>> AnkiRestRepo.service[CirceEntitySerDes]
  }
}
