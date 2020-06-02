package com.gederin.zookeeper.watcher;

import com.gederin.config.Config;
import com.gederin.service.ClusterInformationService;
import com.gederin.zookeeper.service.ZookeeperService;

import org.I0Itec.zkclient.IZkStateListener;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.springframework.stereotype.Component;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConnectStateChangeListener implements IZkStateListener {
    private final ZookeeperService zookeeperService;
    private final ClusterInformationService clusterInformationService;
    private final Config config;

    @Override
    public void handleStateChanged(KeeperState keeperState) throws Exception {
        log.info("current state: {}", keeperState.name());
    }

    @Override
    public void handleNewSession() throws Exception {
        log.info("connected to zookeeper");

        /**
         * Add new znode to /live_nodes and update local cluster information
         */
        zookeeperService.createAndAddToLiveNodes(config.getHostPort(), "cluster node");

        List<String> liveNodes = zookeeperService.getLiveNodesInZookeeperCluster();
        clusterInformationService.rebuildLiveNodesList(liveNodes);

        /**
         * Retry creating znode under /election
         */
        zookeeperService.createNodeInElectionZnode(config.getHostPort());
        clusterInformationService.setMasterNode(zookeeperService.getLeaderNodeData());
    }

    @Override
    public void handleSessionEstablishmentError(Throwable throwable) {
        log.error("could not establish zookeeper session");
    }
}
