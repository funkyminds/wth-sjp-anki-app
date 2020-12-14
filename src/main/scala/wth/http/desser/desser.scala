package wth.http

import io.circe._
import io.circe.generic.auto._
import org.http4s._
import wth.model.anki.deck._
import wth.model.anki.note._
import wth.model.http.CirceEntitySerDes
import zio._
import zio.interop.catz._

package object desser {

  val encoders: ULayer[Has[(CirceEntitySerDes[AddNote], CirceEntitySerDes[CreateDeck])]] =
    ZLayer.succeed {
      val addNoteSerDes: CirceEntitySerDes[AddNote] = new CirceEntitySerDes[AddNote] {

        implicit def decoderCirce(implicit decoder: Decoder[AddNote]): EntityDecoder[Task, AddNote] =
          circe.jsonOf[Task, AddNote]

        implicit def encoderCirce(implicit decoder: Encoder[AddNote]): EntityEncoder[Task, AddNote] =
          circe.jsonEncoderOf[Task, AddNote]

        implicit override def encoder: EntityEncoder[Task, AddNote] = encoderCirce

        implicit override def decoder: EntityDecoder[Task, AddNote] = decoderCirce
      }

      val createDeckSerDer = new CirceEntitySerDes[CreateDeck] {

        implicit def decoderCirce(implicit decoder: Decoder[CreateDeck]): EntityDecoder[Task, CreateDeck] =
          circe.jsonOf[Task, CreateDeck]

        implicit def encoderCirce(implicit decoder: Encoder[CreateDeck]): EntityEncoder[Task, CreateDeck] =
          circe.jsonEncoderOf[Task, CreateDeck]

        implicit override def encoder: EntityEncoder[Task, CreateDeck] = encoderCirce

        implicit override def decoder: EntityDecoder[Task, CreateDeck] = decoderCirce
      }

      (addNoteSerDes, createDeckSerDer)
    }
}
