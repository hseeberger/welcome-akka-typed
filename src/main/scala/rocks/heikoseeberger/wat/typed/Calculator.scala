/*
 * Copyright 2018 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rocks.heikoseeberger.wat
package typed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, SupervisorStrategy, Terminated }

object Calculator {

  sealed trait Command

  final case class Add(n: Int)                        extends Command
  final case class Subtract(n: Int)                   extends Command
  final case class Multiply(n: Int)                   extends Command
  final case class Divide(n: Int)                     extends Command
  final case class GetValue(replyTo: ActorRef[Value]) extends Command
  final case class Value(value: Int)

  def apply(value: Int): Behavior[Command] =
    Behaviors.receiveMessage {
      case Add(n)            => Calculator(value + n)
      case Subtract(n)       => Calculator(value - n)
      case Multiply(n)       => Calculator(value * n)
      case Divide(n)         => Calculator(value / n)
      case GetValue(replyTo) => replyTo ! Value(value); Behaviors.same
    }
}

object CalculatorMain {
  import Calculator._

  sealed trait Command
  private final case class HandleValue(value: Int) extends Command

  def main(args: Array[String]): Unit =
    ActorSystem(CalculatorMain(), "calculator")

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      val calculator =
        context.spawn(
//          Behaviors
//            .supervise(Calculator(0))
//            .onFailure[ArithmeticException](SupervisorStrategy.resume.withLoggingEnabled(false)),
          Calculator(0),
          "calculator"
        )
      context.watch(calculator)

      calculator ! Add(42)
      calculator ! Divide(0)
      calculator ! GetValue(context.messageAdapter(v => HandleValue(v.value)))

      Behaviors
        .receive[Command] {
          case (_, HandleValue(value)) =>
            println(s"Calculator value = $value")
            Behaviors.stopped
        }
        .receiveSignal {
          case (_, Terminated(`calculator`)) =>
            println("Stopping because calculator terminated")
            Behaviors.stopped
        }
    }
}
