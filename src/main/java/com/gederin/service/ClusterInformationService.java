package com.gederin.service;

import com.gederin.repository.ClusterInformationRepository;

import org.springframework.stereotype.Service;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClusterInformationService {

    private final ClusterInformationRepository clusterInformationRepository;

    public void rebuildAllNodesList(List<String> allNodes) {
        clusterInformationRepository.getAllClusterNodes().clear();
        clusterInformationRepository.getAllClusterNodes().addAll(allNodes);
    }

    public void rebuildLiveNodesList(List<String> liveNodes) {
        clusterInformationRepository.getLiveClusterNodes().clear();
        clusterInformationRepository.getLiveClusterNodes().addAll(liveNodes);
    }

    public void setMasterNode(String node) {
        clusterInformationRepository.setMasterNode(node);
    }
}
