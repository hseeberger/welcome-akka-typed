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
package mixed

import akka.actor.{ Actor, ActorSystem, Props, Terminated }
import akka.typed.ActorRef
import de.heikoseeberger.wat.typed.Account
import akka.typed.scaladsl.adapter._

object Transfer {

  def apply(amount: Long, from: ActorRef[Account.Command], to: ActorRef[Account.Command]): Props =
    Props(new Transfer(amount, from, to))
}

final class Transfer(amount: Long, from: ActorRef[Account.Command], to: ActorRef[Account.Command])
    extends Actor {

  from ! Account.Withdraw(amount, self)

  override def receive = {
    case Account.InsufficientBalance(_) =>
      println("Aborting transfer because insufficient balance!")
      context.stop(self)

    case Account.Withdrawn =>
      to ! Account.Deposit(amount, self)
      context.become(withdrawn)
  }

  private def withdrawn: Receive = {
    case Account.Deposited =>
      println("Transfer done")
      context.stop(self)
  }
}

object TransferMain {

  final class Root extends Actor {
    private val from     = context.spawn(Account(100), "from")
    private val to       = context.spawn(Account(100), "to")
    private val transfer = context.actorOf(Transfer(150, from, to), "transfer")

    context.watch(transfer)

    override def receive = {
      case Terminated(`transfer`) => context.system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("transfer")
    system.actorOf(Props(new Root), "root")
  }
}
