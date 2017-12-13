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

import akka.typed.{ ActorRef, Behavior }
import akka.typed.scaladsl.Actor

/**
  * A typed actor with an immutable behavior, i.e. one that doesn't close over mutable state.
  */
object Account {

  sealed trait Command

  final case class GetBalance(replyTo: ActorRef[Balance]) extends Command
  final case class Balance(balance: Long)

  final case class Deposit(amount: Long, replyTo: ActorRef[Deposited.type]) extends Command
  final case object Deposited

  final case class Withdraw(amount: Long, replyTo: ActorRef[WithdrawReply]) extends Command
  sealed trait WithdrawReply
  final case class InsufficientBalance(balance: Long) extends WithdrawReply
  final case object Withdrawn                         extends WithdrawReply

  def apply(balance: Long = 0): Behavior[Command] =
    Actor.immutable {
      case (_, GetBalance(replyTo)) =>
        replyTo ! Balance(balance)
        Actor.same

      case (_, Deposit(amount, replyTo)) =>
        replyTo ! Deposited
        Account(balance + amount)

      case (_, Withdraw(amount, replyTo)) =>
        if (balance < amount) {
          replyTo ! InsufficientBalance(balance)
          Actor.same
        } else {
          replyTo ! Withdrawn
          Account(balance - amount)
        }
    }
}
