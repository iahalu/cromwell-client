package group.research.aging.cromwell.client

import java.net.URI

import _root_.akka.http.scaladsl.HttpExt
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.effect.{ContextShift, IO}
import hammock.InterpTrans
import hammock.akka.AkkaInterpreter
import hammock.apache.ApacheInterpreter
import io.circe.generic.JsonCodec
import sttp.client.SttpBackend
import sttp.client.akkahttp.AkkaHttpBackend
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source, StreamConverters}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import sttp.client._
import sttp.client.circe._
import sttp.client.akkahttp._
import akka.http.scaladsl.model.ws.{Message, WebSocketRequest}
import akka.http.scaladsl.settings.{ConnectionPoolSettings, PoolImplementation}

import scala.concurrent.{ExecutionContext, Future}

object CromwellClient {


  lazy val defaultServerPort = "8000"

  lazy val localhost: CromwellClient = new CromwellClient(s"http://localhost:${defaultServerPort}", "v1")

  lazy val defaultURL: String = scala.util.Properties.envOrElse("CROMWELL", s"http://localhost:${defaultServerPort}" )

  lazy val defaultClientPort: String = scala.util.Properties.envOrElse("CROMWELL_CLIENT_PORT", "8001")

  lazy val defaultHost: String = new URI(defaultURL).getHost

  lazy val  default: CromwellClient = new CromwellClient(defaultURL, "v1")

  def apply(base: String): CromwellClient = new CromwellClient(base, "v1")

}


@JsonCodec case class CromwellClient(base: String, version: String = "v1") extends CromwellClientShared with RosHttp
{
  implicit override protected def getInterpreter: InterpTrans[IO] = ApacheInterpreter.instance
}

case class CromwellClientAkka(base: String, version: String = "v1")(implicit val http: HttpExt, val materializer: ActorMaterializer)
                             extends CromwellClientShared with CromwellClientJVMSpecific with PostSttp {
  implicit val executionContext: ExecutionContext = http.system.dispatcher
  implicit val cs: ContextShift[IO] = IO.contextShift(executionContext)

  implicit override protected def getInterpreter: InterpTrans[IO] = AkkaInterpreter.instance[IO]//(http)

  override implicit def sttpBackend: SttpBackend[Future, Source[ByteString, Any], Flow[Message, Message, *]] =
    AkkaHttpBackend.usingActorSystem(http.system,
      options = SttpBackendOptions.Default
    )
}