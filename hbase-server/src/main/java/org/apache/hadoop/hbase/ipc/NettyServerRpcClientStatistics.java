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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.hadoop.hbase.HBaseInterfaceAudience;
import org.apache.yetus.audience.InterfaceAudience;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.hbase.thirdparty.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.hbase.thirdparty.io.netty.channel.Channel;
import org.apache.hbase.thirdparty.io.netty.util.AttributeKey;

import org.apache.hadoop.hbase.shaded.protobuf.generated.ClusterStatusProtos;

@InterfaceAudience.LimitedPrivate({ HBaseInterfaceAudience.CONFIG })
public class NettyServerRpcClientStatistics {

  public static final Logger LOG = LoggerFactory.getLogger(NettyServerRpcClientStatistics.class);
  private static NettyRpcServer RPC_SERVER;

  public static final AttributeKey<ChannelAttributes> CHANNEL_CLIENT_ATTR_KEY =
    AttributeKey.valueOf("ClientInfo");
  private String channelServerInfo;
  private List<ClusterStatusProtos.ClientInfo> currentClientInfos = new ArrayList<>();
  private final ScheduledExecutorService scheduleThreadPool = Executors.newScheduledThreadPool(1,
    new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ClientInfoStatistics").build());

  public void initialize(NettyRpcServer rpcServer, InetSocketAddress bindAddress)
    throws UnknownHostException {
    RPC_SERVER = rpcServer;
    InetAddress ia = InetAddress.getLocalHost();
    String host = ia.getHostName();
    String port = String.valueOf(bindAddress.getPort());
    channelServerInfo = host + ":" + port + ":" + RPC_SERVER.getClass().getSimpleName();
    LOG.info("Server channel info is {}", channelServerInfo);
    scheduleThreadPool.scheduleAtFixedRate(this::refreshClientConnectionInfo, 5, 10,
      TimeUnit.SECONDS);
  }

  public void refreshClientConnectionInfo() {
    LOG.info("total {} channels", RPC_SERVER.allChannels.size());
    List<ClusterStatusProtos.ClientInfo> clientInfos = new ArrayList<>();
    Map<String, ClusterStatusProtos.ClientInfo.Builder> clientInfoBuilders = new HashMap<>();

    for (Channel ch : RPC_SERVER.allChannels) {
      ChannelAttributes attr = ch.attr(CHANNEL_CLIENT_ATTR_KEY).get();

      if (attr != null) {
        String uniqKey = attr.convertUniqKey();
        ClusterStatusProtos.ClientInfo.Builder builder =
          clientInfoBuilders.computeIfAbsent(uniqKey, key -> {
            ClusterStatusProtos.ClientInfo.Builder tmpbuilder =
              ClusterStatusProtos.ClientInfo.newBuilder();
            tmpbuilder.setClientIp(attr.getClientIP());
            tmpbuilder.setClientVersion(attr.getVersionInfo());
            tmpbuilder.setUserName(attr.getUserName());
            tmpbuilder.setAuth(attr.getAuthenticationMethod());
            tmpbuilder.setServiceName(attr.getServiceName());
            tmpbuilder.setServerInfo(channelServerInfo);
            tmpbuilder.setAuth(attr.getAuthenticationMethod());
            return tmpbuilder;
          });
        builder.addClientPorts(attr.getClientPort());
      }
    }

    for (ClusterStatusProtos.ClientInfo.Builder clientBuilder : clientInfoBuilders.values()) {
      int socketNum = clientBuilder.getClientPortsList().size();
      clientBuilder.setSocketNum(socketNum);
      clientInfos.add(clientBuilder.build());
    }

    currentClientInfos = clientInfos;
  }

  public List<ClusterStatusProtos.ClientInfo> getCurrentClientInfos() {
    return currentClientInfos;
  }

  public void stop() {
    this.scheduleThreadPool.shutdown();
  }
}
