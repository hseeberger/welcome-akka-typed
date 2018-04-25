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
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, Terminated }

/**
  * A typed actor for a transfer:
  *   - First send `Withdraw` to `from`.
  *   - Then abort if the reply is `InsufficientBalance`,
  *   - else send `Deposit` to `to`.
  *   - Finally stop after receiving `Deposited`.
  */
object Transfer {
  import Account._

  sealed trait Command

  private final case object HandleInsufficientBalance extends Command
  private final case object HandleWithdrawn           extends Command
  private final case object HandleDeposited           extends Command

  def apply(amount: Long, from: ActorRef[Withdraw], to: ActorRef[Deposit]): Behavior[Command] =
    Behaviors.setup { context =>
      from ! Withdraw(amount, context.messageAdapter({
        case InsufficientBalance => HandleInsufficientBalance
        case Withdrawn           => HandleWithdrawn
      }))

      Behaviors.receivePartial {
        case (_, HandleInsufficientBalance) =>
          println(s"Aborting transfer of $amount because of insufficient balance!")
          Behaviors.stopped

        case (context, HandleWithdrawn) =>
          to ! Deposit(amount, context.messageAdapter(_ => HandleDeposited))

          Behaviors.receiveMessagePartial {
            case HandleDeposited =>
              println("Transfer done")
              Behaviors.stopped
          }
      }
    }
}

object TransferMain {

  sealed trait Command

  def main(args: Array[String]): Unit =
    ActorSystem(TransferMain(), "transfer")

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      val from     = context.spawn(Account(100), "from")
      val to       = context.spawn(Account(100), "to")
      val transfer = context.spawn(Transfer(50, from, to), "transfer")

      context.watch(transfer)

      Behaviors.receiveSignal { case (_, Terminated(`transfer`)) => Behaviors.stopped }
    }
}
