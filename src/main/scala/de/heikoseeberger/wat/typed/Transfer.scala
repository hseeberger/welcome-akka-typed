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

package de.heikoseeberger.wat
package typed

import akka.typed.scaladsl.Actor
import akka.typed.{ ActorRef, ActorSystem, Behavior, Terminated }

/**
  * A typed actor for a transfer:
  *   - First send `Withdraw` to `from`.
  *   - Then abort if the reply is `HandleInsufficientBalance`,
  *   - else send `Deposit` to `to`.
  *   - Finally stop after receiving `Deposited`.
  */
object Transfer {
  import Account._

  sealed trait Command

  private final case class HandleInsufficientBalance(balance: Long) extends Command
  private final case object HandleWithdrawn                         extends Command
  private final case object HandleDeposited                         extends Command

  def apply(amount: Long, from: ActorRef[Withdraw], to: ActorRef[Deposit]): Behavior[Command] =
    Actor.deferred { context =>
      from ! Withdraw(amount, context.spawnAdapter({
        case InsufficientBalance(balance) => HandleInsufficientBalance(balance)
        case Withdrawn                    => HandleWithdrawn
      }))

      Actor.immutablePartial {
        case (_, HandleInsufficientBalance(balance)) =>
          println(s"Aborting transfer of $amount because of insufficient balance $balance!")
          Actor.stopped

        case (context, HandleWithdrawn) =>
          to ! Deposit(amount, context.spawnAdapter(_ => HandleDeposited))

          Actor.immutablePartial {
            case (_, HandleDeposited) =>
              println("Transfer done")
              Actor.stopped
          }
      }
    }
}

object TransferMain {

  sealed trait Command

  def main(args: Array[String]): Unit =
    ActorSystem(TransferMain(), "transfer-main")

  def apply(): Behavior[Command] =
    Actor.deferred { context =>
      val from     = context.spawn(Account(100), "from")
      val to       = context.spawn(Account(100), "to")
      val transfer = context.spawn(Transfer(50, from, to), "transfer")

      context.watch(transfer)
      Actor.onSignal { case (_, Terminated(`transfer`)) => Actor.stopped }
    }
}
