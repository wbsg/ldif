/*
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.util

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 4:21 PM
 */

trait ReportRegister extends Register[ReportPublisher] {
  
  def getRunningJobs : IndexedSeq[ReportPublisher] = {
    getPublisherWithStatus
      .filterNot(_.getStatus.get.equals("Done"))
      .filterNot(_.getStatus.get.equals("Skipped"))
      .filterNot(_.getStatus.get.equals("Failed"))
  }

  def getCompleteJobs : IndexedSeq[ReportPublisher] = {
    getPublisherWithStatus
      .filter(_.getStatus.get.equals("Done"))
  }

  def getFailedJobs : IndexedSeq[ReportPublisher] = {
    getPublisherWithStatus
      .filter(_.getStatus.get.equals("Failed"))
  }

  def getPublisherWithStatus : IndexedSeq[ReportPublisher] = {
    getPublishers().filterNot(_.getStatus==None)
  }

}