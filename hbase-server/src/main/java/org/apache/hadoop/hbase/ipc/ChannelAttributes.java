/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.ipc;

import org.apache.hadoop.hbase.HBaseInterfaceAudience;
import org.apache.yetus.audience.InterfaceAudience;
import org.apache.yetus.audience.InterfaceStability;

/**
 * module class for client info
 */
@InterfaceAudience.LimitedPrivate({ HBaseInterfaceAudience.COPROC, HBaseInterfaceAudience.PHOENIX })
@InterfaceStability.Evolving
public class ChannelAttributes {

  String userName;
  String authenticationMethod;
  String versionInfo;
  String startTime;
  String clientIP;
  Integer clientPort;
  String serviceName;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getAuthenticationMethod() {
    return authenticationMethod;
  }

  public void setAuthenticationMethod(String authenticationMethod) {
    this.authenticationMethod = authenticationMethod;
  }

  public String getVersionInfo() {
    return versionInfo;
  }

  public void setVersionInfo(String versionInfo) {
    this.versionInfo = versionInfo;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getClientIP() {
    return clientIP;
  }

  public void setClientIP(String clientIP) {
    this.clientIP = clientIP;
  }

  public Integer getClientPort() {
    return clientPort;
  }

  public void setClientPort(Integer clientPort) {
    this.clientPort = clientPort;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  @Override
  public String toString() {
    return String.format(
      "clientIp=%s, clientPort=%s, username=%s, auth=%s, version=%s, serviceName=%s, startTime=%s",
      clientIP, clientPort, userName, authenticationMethod, versionInfo, serviceName, startTime);
  }

  public String convertUniqKey() {
    return clientIP + userName + versionInfo + serviceName;
  }

}
