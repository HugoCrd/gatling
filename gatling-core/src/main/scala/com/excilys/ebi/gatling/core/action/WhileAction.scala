/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.gatling.core.action

import scala.annotation.implicitNotFound

import com.excilys.ebi.gatling.core.session.handler.{TimerBasedIterationHandler, CounterBasedIterationHandler}
import com.excilys.ebi.gatling.core.session.Session

import akka.actor.ActorRef

/**
 * Action in charge of controlling a while loop execution.
 *
 * @constructor creates a While loop in the scenario
 * @param condition the condition that decides when to exit the loop
 * @param loopNext the chain executed if condition evaluates to true, passed as a Function for build time
 * @param next the chain executed if testFunction evaluates to false
 * @param counterName the name of the counter for this loop
 */
class WhileAction(condition: Session => Boolean, loopNext: ActorRef => ActorRef, next: ActorRef, val counterName: String) extends Action with TimerBasedIterationHandler with CounterBasedIterationHandler {

	val loopNextAction = loopNext(self)

	/**
	 * Evaluates the condition and if true executes the first action of loopNext
	 * else it executes next
	 *
	 * @param session the session of the virtual user
	 */
	def execute(session: Session) {

		val sessionWithTimerIncremented = increment(init(session))

		if (condition(sessionWithTimerIncremented))
			loopNextAction ! sessionWithTimerIncremented
		else
			next ! expire(sessionWithTimerIncremented)
	}
}