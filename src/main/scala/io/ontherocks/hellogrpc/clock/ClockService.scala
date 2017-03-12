/*
 * Copyright 2016 Petra Bierleutgeb
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

package io.ontherocks.hellogrpc
package clock

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ Executors, TimeUnit }

import io.grpc.stub.StreamObserver

object ClockService {

  val RepeatForSeconds = 10
  val InitialDelayMs   = 0L
  val IntervalMs       = 1000L

}

class ClockService extends ClockGrpc.Clock {

  import ClockService._

  /**
    * Returns the current time in milliseconds every second for the next 10 seconds.
    */
  def getTime(request: TimeRequest, responseObserver: StreamObserver[TimeResponse]): Unit = {
    val scheduler = Executors.newSingleThreadScheduledExecutor()
    val tick = new Runnable {
      val counter = new AtomicInteger(RepeatForSeconds)
      def run() =
        if (counter.getAndDecrement() >= 0) {
          val currentTime = System.currentTimeMillis()
          responseObserver.onNext(TimeResponse(currentTime))
        } else {
          scheduler.shutdown()
          responseObserver.onCompleted()
        }
    }
    scheduler.scheduleAtFixedRate(tick, InitialDelayMs, IntervalMs, TimeUnit.MILLISECONDS)
  }

}
