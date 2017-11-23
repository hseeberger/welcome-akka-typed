/*
 * Copyright 2017 Heiko Seeberger
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

package de.heikoseeberger.wat.typed

import akka.typed.scaladsl.Actor
import akka.typed.{ ActorRef, ActorSystem, Behavior, SupervisorStrategy, Terminated }

object Calculator {

  sealed trait Command

  final case class Add(n: Int)                        extends Command
  final case class Subtract(n: Int)                   extends Command
  final case class Multiply(n: Int)                   extends Command
  final case class Divide(n: Int)                     extends Command
  final case class GetValue(replyTo: ActorRef[Value]) extends Command
  final case class Value(value: Int)

  def apply(value: Int): Behavior[Command] =
    Actor.immutable {
      case (_, Add(n))      => Calculator(value + n)
      case (_, Subtract(n)) => Calculator(value - n)
      case (_, Multiply(n)) => Calculator(value * n)
      case (_, Divide(n))   => Calculator(value / n)
      case (_, GetValue(replyTo)) =>
        replyTo ! Value(value)
        Actor.same
    }
}

object CalculatorMain {
  import Calculator._

  sealed trait Command
  private final case class HandleValue(value: Int) extends Command

  def main(args: Array[String]): Unit =
    ActorSystem(CalculatorMain(), "calculator-main")

  def apply(): Behavior[Command] =
    Actor.deferred { context =>
      val calculator =
        context.spawn(
          Actor
            .supervise(Calculator(0))
            .onFailure[ArithmeticException](SupervisorStrategy.resume.withLoggingEnabled(false)),
          "calculator"
        )
      context.watch(calculator)

      calculator ! Add(42)
      calculator ! Divide(0)
      calculator ! GetValue(context.spawnAdapter(v => HandleValue(v.value)))

      Actor
        .immutable[Command] {
          case (_, HandleValue(value)) =>
            println(s"Calculator value = $value")
            Actor.stopped
        }
        .onSignal {
          case (_, Terminated(`calculator`)) =>
            println("Stopping because calculator terminated")
            Actor.stopped
        }
    }
}
