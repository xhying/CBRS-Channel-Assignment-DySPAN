/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spectrumaccesssystem.algo.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import spectrumaccesssystem.algo.DCAConstants;
import spectrumaccesssystem.algo.device.CBSDDevice;
import spectrumaccesssystem.algo.device.Channel;
import spectrumaccesssystem.utils.LogUtils;

/**
 *
 * @author Xuhang Ying <xhying@uw.edu>
 */
public class ConflictGraph{    

    private final String mode;
    private List<CBSDDevice> cbsdDeviceList;
    private String rewardFunc;
    private String penaltyFunc;
    private double alphaLimit;
    private final boolean isDebug;
    
    private List<Node> nodeList;
    private int numOfNodes;
    
    private CarrierSenseGraph csGraph;
    private InterferenceGraph intGraph;
    
    private List<NodeChannelPair> ncPairList;
    private List<NodeChannelPair> superNcPairList;
    private int numOfNcPairs;
    
    // Indexed by ncPairIdx. The i-th element contains a list of neighboring NC pair indices (and penalties). 
    private List< Map<Integer, Double> > edgeList;   // HashMap<neighborNcPairIdx, penalty>
    
    // Indexed by nodeIdx. The i-th element contains a list of indices of NC pairs that belong to node i.
    private List<List<Integer>> ncPairClusters;
    private List<Set<Integer>> unremovedNcPairIdxSetByNodeIdx;
    private List<Set<Integer>> removedNcPairIdxSetByNodeIdx;
    private boolean[] cardinalityConstraint; 
    
    private Set<Integer> unremovedNcPairIdxSet;
    
    // ** Min-demand NC pairs **
    private List<Integer> minDemandNcPairIdxList;
    private Map<Integer, List<Integer>> minDemandEdgeDict;
    private Set<Integer> unremovedMinDemandNcPairIdxSet;
    
    // For super-NC pairs
    private Set<Integer> chIdSet;
    Map<Integer, List<List<Integer>>> superNodesByChId;
    
    public ConflictGraph (){
        this.cbsdDeviceList = null;
        this.rewardFunc = null;
        this.penaltyFunc = null;
        this.isDebug = false;
        this.mode = null;
    }
    
    // Binary conflict graph
    public ConflictGraph ( String mode, List<CBSDDevice> cbsdDeviceList, double alphaLimit, 
            String rewardFunc, boolean isDebug){
        
        if (!mode.equals(DCAConstants.CONFLICT_GRAPH_BINARY)){
            throw new IllegalArgumentException("This mode must be binary");
        }

        this.mode = mode;
        this.cbsdDeviceList = cbsdDeviceList;
        this.alphaLimit = alphaLimit;   // 0 means disabling super-node formation.
        this.rewardFunc = rewardFunc;
        this.isDebug = isDebug;

        initBinaryConflictGraph();
    }
    
    // Non-binary conflict graph
    public ConflictGraph ( String mode, List<CBSDDevice> cbsdDeviceList, 
            String rewardFunc, String penaltyFunc, boolean isDebug){
        
        if (!mode.equals(DCAConstants.CONFLICT_GRAPH_NON_BINARY )){
            throw new IllegalArgumentException("This mode must be nonbinary");
        }

        this.mode = mode;
        this.cbsdDeviceList = cbsdDeviceList;
        this.rewardFunc = rewardFunc;
        this.penaltyFunc = penaltyFunc;
        this.isDebug = isDebug;

        initNonBinaryConflictGraph();
    }
    
    private void initBinaryConflictGraph(){
        nodeList = createNodeList(cbsdDeviceList);
        numOfNodes = nodeList.size();
        System.out.println("[CG] Num of nodes = " + numOfNodes);
        
        if (isDebug){nodeList.stream().forEach((node) -> { node.print(); });}
        
        csGraph = createCsGraph(nodeList, isDebug);
        intGraph = createIntGraph(nodeList, isDebug);
        
        ncPairList = createNcPairList(nodeList);
        
        // ======================================
        // ** Super-NC pairs **
        chIdSet = findChIdSet(ncPairList);
        superNodesByChId = createSuperNodesByChId(nodeList, chIdSet, csGraph, alphaLimit);
        superNcPairList = createSuperNcPairList(nodeList, superNodesByChId, rewardFunc);
        ncPairList.addAll(superNcPairList);
        // ======================================
        
        numOfNcPairs = ncPairList.size();
        for(int i = 0; i < ncPairList.size(); i++) { 
            ncPairList.get(i).setNcPairIdx(i);      // Important
        }
        System.out.println("[CG] Num of NC Pairs = " + ncPairList.size());
        
        if (isDebug){ ncPairList.stream().forEach( (ncPair) -> { ncPair.print();});}
        
        ncPairClusters = createNcPairClusters(ncPairList, numOfNodes);
        unremovedNcPairIdxSet = createUnremovedNcPairIdxSet(numOfNcPairs);
        edgeList = createEdgeList(numOfNodes, numOfNcPairs, ncPairList, ncPairClusters, intGraph);
        
        // ======================================
        // ** Min-demand NC pairs *
        minDemandNcPairIdxList = createMinDemandNcPairIdxList(ncPairList);
        minDemandEdgeDict = createMinDemandEdgeDict(ncPairList, edgeList);
        unremovedMinDemandNcPairIdxSet = createUnremovedMinDemandNcPairIdxSet(minDemandNcPairIdxList);
        // ======================================
        
        // Remove edges between NC pairs associated with the same super-NC pair.
        updateEdgesForSuperNcPairs();
    }
    
    private void initNonBinaryConflictGraph(){
        nodeList = createNodeList(cbsdDeviceList);
        numOfNodes = nodeList.size();
        System.out.println("[CG] Num of nodes = " + numOfNodes);
        if (isDebug){nodeList.stream().forEach((node) -> { node.print(); });}
        
        ncPairList = createNcPairList(nodeList);
        numOfNcPairs = ncPairList.size();
        for(int i = 0; i < ncPairList.size(); i++) { 
            ncPairList.get(i).setNcPairIdx(i);      // Important
        }
        System.out.println("[CG] Num of NC Pairs = " + ncPairList.size());
        if (isDebug){ ncPairList.stream().forEach( (ncPair) -> { ncPair.print();});}
        
        // NC pairs that belong to the same node are grouped as a cluster.
        ncPairClusters = createNcPairClusters(ncPairList, numOfNodes);      
        unremovedNcPairIdxSetByNodeIdx = createUnremovedNcPairIdxSetByNodeIdx(ncPairClusters);
        removedNcPairIdxSetByNodeIdx = createRemovedNcPairIdxSetByNodeIdx(ncPairClusters);
        
        // Identify super-NC pairs, but do NOT add them to the graph.
        chIdSet = findChIdSet(ncPairList);
        superNodesByChId = createSuperNodesByChId(nodeList, chIdSet, csGraph, alphaLimit);
        superNcPairList = createSuperNcPairList(nodeList, superNodesByChId, rewardFunc);
        
        // Create non-binary edge list (List< Map<Integer, Double> >) without intr-node edges.
        edgeList = createEdgeList2(numOfNodes, numOfNcPairs, ncPairList, superNcPairList, ncPairClusters, intGraph);
    
        // Set penalties between NC pairs associated with the same super-NC pair to 0. 
        updateEdgesForSuperNcPairs();
    }
    
    private List<Node> createNodeList(List<CBSDDevice> cbsdDeviceList){
        List<Node> list = new ArrayList<>();
        cbsdDeviceList.stream().forEach((cbsd) -> {
            int nodeIdx = list.size();
            list.add(createNodeFromCbsdDevice(nodeIdx, cbsd));
        });
        return list;
    }
    
    private CarrierSenseGraph createCsGraph(List<Node> list, boolean debug){
        return new CarrierSenseGraph(list, debug);
    }
    
    private InterferenceGraph createIntGraph(List<Node> list, boolean debug){
        return new InterferenceGraph(list, debug);
    }
    
    private List<NodeChannelPair> createNcPairList(List<Node> nodeList){
        List<NodeChannelPair> list = new ArrayList<>();
        for (Node node : nodeList){
            List<NodeChannelPair> list2 = node.getAvaialbleNcPairList();
            list.addAll(list2);
        }
        return list;
    }
    
    private Set<Integer> findChIdSet(List<NodeChannelPair> ncPairlist){
        Set<Integer> set = new HashSet<>();
        ncPairlist.stream().forEach((pair) -> { 
            int chId = pair.getChannel().getChId();
            
            if (isDebug){
                System.out.format("chId = %d, startChn = %d, endChn = %d\n", chId,
                        Channel.findStartChn(chId), Channel.findEndChn(chId));
            }
            
            set.add(chId); 
        });
        return set;
    }
    
    private Map<Integer, List<List<Integer>>> createSuperNodesByChId(List<Node> nodeList, 
            Set<Integer> chIdSet, CarrierSenseGraph csGraph, double alpha){
        Map<Integer, List<List<Integer>>> map = new HashMap<>();
        
        if (isDebug){ 
            System.out.println("[DEBUG] Possible channel assignments = ");
            System.out.println(new ArrayList<>(chIdSet).toString());
        }
        
        chIdSet.stream().forEach((chId) -> {
            // Identify a set of nodes that can coexist on chId
            List<Integer> nodeIdxList = new ArrayList<>();
            nodeList.stream().forEach((node)->{
                if (node.checkAvailability(Channel.findStartChn(chId), Channel.findEndChn(chId))){
                    nodeIdxList.add(node.getNodeIdx());
                }
            });
            
            // Identify the CS subgraph for nodeIdxList.
            // Need to construct a string that describes the topology for the Bron-Kerbosch algorithm.
            int numOfEdges = 0;
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < nodeIdxList.size(); i++){
                for(int j = i+1; j < nodeIdxList.size(); j++){
                    int nodeIdx1 = nodeIdxList.get(i);
                    int nodeIdx2 = nodeIdxList.get(j);
                    
                    if (csGraph.isWithinCsRange(nodeIdx1, nodeIdx2)){
                        numOfEdges ++;
                        sb.append(String.format("%d %d\n", i, j));  // Nodes have been relabled w.r.t. nodeIdxList.
                    }
                }
            }
            
            sb.insert(0, String.format("1\n%d\n%d\n", nodeIdxList.size(), numOfEdges));
            
            // Prepare activity indices
            List<Double> activityIdxList = new ArrayList<>();
            for(int i = 0; i < nodeIdxList.size(); i++){
                int nodeIdx = nodeIdxList.get(i);
                // Nodes have been implicitly relabled by ordering in nodeIdxList.
                activityIdxList.add(nodeList.get(nodeIdx).getActivityIdx(chId));
            }
            
            if (isDebug){ 
                System.out.println("[DEBUG] Activity Idx = " + activityIdxList.toString()); 
            }
            
            /** Super-node formation. */
            List<List<Integer>> list = SuperNodeFormation.getSuperNodeList(sb.toString(), activityIdxList, alpha);
            
            // Convert indices back to nodeIdx.
            List<List<Integer>> superNodeList = new ArrayList<>();
            for(int i = 0; i < list.size(); i++){
                superNodeList.add(i, new ArrayList<>());
                List<Integer> idxList = list.get(i);
                // Find the corresponding nodeIdx for each element in idxList.
                for(int j = 0; j < idxList.size(); j++){
                    // idxList.get(j) is local index
                    // nodeIdxList.get(idxList.get(j)) is the corresponding nodeIdx
                    superNodeList.get(i).add(nodeIdxList.get(idxList.get(j)));
                }
            }

            if (isDebug){
                for(int i = 0; i < superNodeList.size(); i++){
                    System.out.format("[DEBUG] chId = %d ([%d, %d]), superNode %d = %s\n",
                            chId, Channel.findStartChn(chId), Channel.findEndChn(chId), 
                            i, superNodeList.get(i).toString());
                }
            }
            
            map.put(chId, superNodeList);
        });
        
        return(map);
    }
    
    private List<NodeChannelPair> createSuperNcPairList(List<Node> nodeList, 
            Map<Integer, List<List<Integer>>> superNodesByChId, String rewardFunc){
        
        List<NodeChannelPair> list = new ArrayList<>();
        
        superNodesByChId.keySet().stream().forEach((chId) -> {
            Channel channel = new Channel(Channel.findStartChn(chId), Channel.findEndChn(chId));
            
            List<List<Integer>> superNodeList = superNodesByChId.get(chId);
            // A super-node is a list of nodeIdx.
            superNodeList.stream().forEach((superNode)->{   // A super-node is a List<Integer>.
                // Check if this channel is the min. demand of any node in the current super node.
                boolean isMinDemand = false;
                for(int i = 0; i < superNode.size(); i++){
                    if ( (nodeList.get(superNode.get(i)).getMinDemand() == channel.getNumOfChns())){
                        isMinDemand = true;
                        break;
                    }
                }
                
                list.add(new NodeChannelPair(superNode, channel, isMinDemand, rewardFunc));
            });
        });
        
        System.out.println("[CG] Num of Super-NC Pairs = " + list.size());
        
        return list;
    }
    
    private List<List<Integer>> createNcPairClusters(List<NodeChannelPair> ncPairList, int numOfNodes){
        List<List<Integer>> clusters = new ArrayList<>();
        for(int i = 0; i < numOfNodes; i++){
            clusters.add(i, new ArrayList<>());
        }
        // Put each NC pair into corresponding buckets indexed by nodeIdx.
        for(int i = 0; i < ncPairList.size(); i++){            
            List<Integer> nodeIdxList = ncPairList.get(i).getNodeIdxList();
            for(int j = 0; j < nodeIdxList.size(); j++){
                clusters.get(nodeIdxList.get(j)).add(ncPairList.get(i).getNcPairIdx());
            }
        }
        return clusters;
    }
    
    private List<Set<Integer>> createUnremovedNcPairIdxSetByNodeIdx(List<List<Integer>> clusters){
        List<Set<Integer>> list = new ArrayList<>();
        for (int i = 0; i < clusters.size(); i++){
            // Create a set for each node i. 
            list.add(i, new HashSet<>());
            
            // Add NC pair indices belonging to node i to the i-th list.
            for (int idx : clusters.get(i)){
                list.get(i).add(idx);
            }
        }
        return list;
    }
    
    private List<Set<Integer>> createRemovedNcPairIdxSetByNodeIdx(List<List<Integer>> clusters){
        List<Set<Integer>> list = new ArrayList<>();
        for (int i = 0; i < clusters.size(); i++){
            // Create an empty set for each node i. 
            list.add(i, new HashSet<>());
        }
        return list;
    }
    
    private List<Integer> createMinDemandNcPairIdxList(List<NodeChannelPair> ncPairList){
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < ncPairList.size(); i++){            
            // Add it to minDemandNCPairIdxList if it is a min-demand NC pair
            if (ncPairList.get(i).isMinDemand() == true){
                list.add(ncPairList.get(i).getNcPairIdx());
            }
        }
        return list;
    }
    
    private Set<Integer> createUnremovedNcPairIdxSet(int numOfNcPairs){
        Set<Integer> set = new LinkedHashSet<>();
        for (int i = 0; i < numOfNcPairs; i++){
            set.add(i);
        }
        return set;
    }
    
    private List< Map<Integer, Double> > createEdgeList(int numOfNodes, int numOfNcPairs,
            List<NodeChannelPair> ncPairList, List<List<Integer>> ncPairClusters, InterferenceGraph intGraph){
        
        List< Map<Integer, Double> > list = new ArrayList<>();
        for(int i = 0; i < numOfNcPairs; i++){
            // ncPairIdx -> an index list of conflicting NC pairs
            list.add(i, new HashMap<>());
        }
        
        // One-channel-assignment-per-node constraint
        ncPairClusters.stream().forEach((ncPairIdxList)->{
            // list: an index list of all (S)NC pairs related to a particular node.
            for(int i = 0; i < ncPairIdxList.size(); i++){
                for(int j = i+1; j < ncPairIdxList.size(); j++){
                    list.get(ncPairIdxList.get(i)).put(ncPairIdxList.get(j), 0.0);
                    list.get(ncPairIdxList.get(j)).put(ncPairIdxList.get(i), 0.0);
                }
            }
        });
        
        // Interference constraint
        for(int i = 0; i < numOfNodes; i++){
            for(int j = i+1; j < numOfNodes; j++){
                int minChnDist = intGraph.getMinChnDist(i, j);
                
                if ( minChnDist >= 0 ){     // Co-channel (or adj-channel) interference
                    List<Integer> list1 = ncPairClusters.get(i);
                    List<Integer> list2 = ncPairClusters.get(j);
                    
                    for(int ii = 0; ii < list1.size(); ii++){
                        for(int jj = 0; jj < list2.size(); jj++){
                            NodeChannelPair pair1 = ncPairList.get(list1.get(ii));
                            NodeChannelPair pair2 = ncPairList.get(list2.get(jj));
                            
                            // Nodes i and j are already interfering.
                            // Just need to check if channels are overlapping.
                            if ( pair1.getNcPairIdx() != pair2.getNcPairIdx() &&
                                    pair1.getChannel().isConflicting(pair2.getChannel(), minChnDist) ){
                                list.get(pair1.getNcPairIdx()).put(pair2.getNcPairIdx(), 0.0);
                                list.get(pair2.getNcPairIdx()).put(pair1.getNcPairIdx(), 0.0);
                            }
                        }
                    }
                    
                }
            }
        }
        
        return list;
    }
    
    private List< Map<Integer, Double> > createEdgeList2(int numOfNodes, int numOfNcPairs,
            List<NodeChannelPair> ncPairList, List<NodeChannelPair> superNcPairList,
            List<List<Integer>> ncPairClusters, InterferenceGraph intGraph){
        // (1) No intra-node edges, and (2) a penalty of 0 between NC pairs associated with the same super-NC pair. 
        List< Map<Integer, Double> > list = new ArrayList<>();
        for(int i = 0; i < numOfNcPairs; i++){
            // ncPairIdx -> an index list of conflicting NC pairs
            list.add(i, new HashMap<>());
        }
        
        // Interference constraint between NC pairs belonging to two different nodes.
        for(int i = 0; i < numOfNodes; i++){
            for(int j = i+1; j < numOfNodes; j++){
                int minChnDist = intGraph.getMinChnDist(i, j);
                
                if ( minChnDist >= 0 ){     // Co-channel (or adj-channel) interference
                    List<Integer> list1 = ncPairClusters.get(i);
                    List<Integer> list2 = ncPairClusters.get(j);
                    
                    for(int ii = 0; ii < list1.size(); ii++){
                        for(int jj = 0; jj < list2.size(); jj++){
                            NodeChannelPair pair1 = ncPairList.get(list1.get(ii));
                            NodeChannelPair pair2 = ncPairList.get(list2.get(jj));
                            
                            if ( pair1.getNcPairIdx() == pair2.getNcPairIdx()){
                                LogUtils.INSTANCE.writeLog("SEVERE", "Something is wrong");
                            }
                            
                            // TODO: Compute the normalzied penalty (between 0 and 1) between two NC pairs. 
                            list.get(pair1.getNcPairIdx()).put(pair2.getNcPairIdx(), 0.0);
                            list.get(pair2.getNcPairIdx()).put(pair1.getNcPairIdx(), 0.0);
                        }
                    }
                    
                }
            }
        }
        
        return list;
    }
    
    private Map<Integer, List<Integer>> createMinDemandEdgeDict(List<NodeChannelPair> ncPairList, 
            List< Map<Integer, Double> > edgeList){
        Map<Integer, List<Integer>> dict = new HashMap<>();
        
        int targetIdx;
        
        for (NodeChannelPair ncPair : ncPairList){
            targetIdx = ncPair.getNcPairIdx();
            
            if ( ncPair.isMinDemand() && !dict.containsKey(targetIdx)){
                dict.put(targetIdx, new ArrayList<>());
                
                List<Integer> list = new ArrayList(edgeList.get(targetIdx).keySet());
                
                for (int neighborIdx : list){
                    if (targetIdx == neighborIdx){
                        LogUtils.INSTANCE.writeLog("SEVERE", "=============");
                    }
                    
                    if ( ncPairList.get(neighborIdx).isMinDemand() ){
                        dict.get(targetIdx).add(neighborIdx);
                    }
                }
            }
        }
        
        return dict;
    }
    
    private Set<Integer> createUnremovedMinDemandNcPairIdxSet(List<Integer> minDemandNcPairIdxList){
        Set<Integer> set = new LinkedHashSet<>();
        minDemandNcPairIdxList.stream().forEach((idx) -> { set.add(idx); });
        return set;
    }
    
    private void updateEdgesForSuperNcPairs(){
        superNcPairList.stream().forEach((superNcPair)->{
            // List of indices of member NC pairs belonging to the given super-NC pair.
            List<Integer> memberNcPairIdxList = new ArrayList<>();
            
            // Extract information from the super-NC pair.
            List<Integer> nodeIdxList = superNcPair.getNodeIdxList();
            int chId = superNcPair.getChannel().getChId();
            
            // Find the member NC pair index that corresponds to (nodeIdx, chId).
            nodeIdxList.stream().forEach((nodeIdx)->{
                List<Integer> ncPairIdxList = ncPairClusters.get(nodeIdx);
                
                for(int i = 0; i < ncPairIdxList.size(); i++){
                    NodeChannelPair ncPair = ncPairList.get(ncPairIdxList.get(i));
                    
                    // Each member NC pair contains only one node, and the channel must be the same.
                    if ( (ncPair.getNodeListSize() == 1) && (ncPair.getChannel().getChId() == chId) ){
                        memberNcPairIdxList.add(ncPair.getNcPairIdx());
                    }
                }
            });
            
            if (isDebug){
                System.out.format("Super-NC pair, idx=%d, nodeIdxList=%s, chId=%d, num of members=%d\n",
                        superNcPair.getNcPairIdx(), nodeIdxList.toString(), chId, memberNcPairIdxList.size());
                for(int i = 0; i < memberNcPairIdxList.size(); i++){
                    NodeChannelPair pair = ncPairList.get(memberNcPairIdxList.get(i));
                    System.out.format("NC pair, idx=%d, nodeIdx=%d, chId=%d\n",
                            pair.getNcPairIdx(), pair.getNodeIdxList().get(0), pair.getChannel().getChId());
                }
            }
            
            // Remove edges among member NC pairs.
            for(int i = 0; i < memberNcPairIdxList.size(); i++){
                int pairIdx1 = memberNcPairIdxList.get(i);

                for(int j = i + 1; j < memberNcPairIdxList.size(); j++){
                    int pairIdx2 = memberNcPairIdxList.get(j);
                    
                    boolean ret1 = edgeList.get(pairIdx1).containsKey(pairIdx2);
                    boolean ret2 = edgeList.get(pairIdx2).containsKey(pairIdx1);
                    
                    if (mode.equals(DCAConstants.CONFLICT_GRAPH_BINARY)){
                        edgeList.get(pairIdx1).remove(pairIdx2);
                        edgeList.get(pairIdx2).remove(pairIdx1);
                        
                        if ( isDebug && ret1 && ret2){
                            System.out.format("Removed edge between %d and %d\n", pairIdx1, pairIdx2);
                        }
                        
                    }else if (mode.equals(DCAConstants.CONFLICT_GRAPH_NON_BINARY)){
                        // Do NOT remove edges, but set penalties to 0. 
                        if (ret1){
                            edgeList.get(pairIdx1).put(pairIdx2, 0.0);
                        }
                        
                        if (ret2){
                            edgeList.get(pairIdx2).put(pairIdx1, 0.0);
                        }
                        
                        if ( isDebug && ret1 && ret2){
                            System.out.format("Updated edge penalty between %d and %d\n", pairIdx1, pairIdx2);
                        }
                    }
                    
                    
                }
            }
        });
    }
    
    private Node createNodeFromCbsdDevice (int nodeIdx, CBSDDevice cbsdDevice){
        double activityIndex = cbsdDevice.activityIndex;
        boolean coexistenceEnabled = cbsdDevice.coexistenceEnabled;
        
        boolean[] availableChns = new boolean[Channel.TOTAL_NUM_OF_CHANNELS];
        boolean[] assignedChns = new boolean[Channel.TOTAL_NUM_OF_CHANNELS];
        
        System.arraycopy( cbsdDevice.availableChns, 0, availableChns, 0, cbsdDevice.availableChns.length );
        System.arraycopy( cbsdDevice.assignedChns, 0, assignedChns, 0, cbsdDevice.assignedChns.length );
        
        Node node = new Node( nodeIdx, cbsdDevice, activityIndex, coexistenceEnabled, 
                availableChns, assignedChns, rewardFunc);
        
        return node;
    }
    
    /**
     * Count the number of NC pairs for the target NC pair that are
     * (1) conflicting, and (2) not removed. 
     * 
     * @param ncPairIdx
     * @return 
     */
    public int getVertexDegree(int ncPairIdx){        
        ArrayList<Integer> neighborIdxList = new ArrayList(edgeList.get(ncPairIdx).keySet());
        
        int num = 0;
        for (int neighborIdx : neighborIdxList){
            num += ( unremovedNcPairIdxSet.contains(neighborIdx) == true) ? 1 : 0;
        }

        return num;
    }
    
    /**
     * Count the number of NC pairs for a min-demand NC pair that are
     * (1) conflicting, (2) min-demand, and (3) not removed.
     * 
     * @param ncPairIdx
     * @return 
     */
    public int getMinDemandVertexDegree(int ncPairIdx){
        if (ncPairList.get(ncPairIdx).isMinDemand() == false){
            return(-1);
        }else{
            int num = 0;
            List<Integer> minDemandNeighborIdxList = minDemandEdgeDict.get(ncPairIdx);
            for (int neighborIdx : minDemandNeighborIdxList){
                num += ( unremovedNcPairIdxSet.contains(neighborIdx) == true) ? 1 : 0;
            }
            return num;
        }
    }
    
    /** 
     * Find un-removed NC pairs belonging to nodes other than S that are conflicting with v. 
     * 
     * @param ncPairIdx Index of target min-demand NC pair
     */
    public int getInterNodeVertexDegree(int ncPairIdx){
        if (ncPairList.get(ncPairIdx).isMinDemand() == false){
            LogUtils.INSTANCE.writeLog("SEVERE", "Input idx must be min-demand");
            return(-1);
        }else{
            // Count NC pairs that are (1) conflicting with ncPairIdx, and (2) not removed.
            int num = getVertexDegree(ncPairIdx);
            
            // Count NC pairs that are (1) belonging to one of the nodes that form the NC pair 
            // indexed by ncPairIdx, and (2) not removed.
            Set<Integer> ownNcPairIdxSet = getOwnNcPairIdxSet(ncPairIdx); 
            
            return num - (ownNcPairIdxSet.size() - 1);  // Excluding itself
        }
    }
    
    public int getNumOfSuccessors(int ncPairIdx){
        if (ncPairList.get(ncPairIdx).isMinDemand() == false){
            LogUtils.INSTANCE.writeLog("SEVERE", "Input idx must be min-demand");
            return(-1);
        }else{
            int num = 0;
            Set<Integer> ownNcPairIdxSet = getOwnNcPairIdxSet(ncPairIdx);
            
            Iterator<Integer> it = ownNcPairIdxSet.iterator();
            
            while (it.hasNext()){
                int idx = it.next();
                num += ((idx != ncPairIdx) && checkSuccessor(ncPairIdx, idx))? 1 : 0;
            }
            
            return num;
        }
    }
    
    /**
     * Get the index set of NC pairs that (1) belong to one of the nodes
     * that form the NC pair indexed by ncPairIdx, and (2) not removed. 
     * 
     * @param ncPairIdx
     * @return 
     */
    public Set<Integer> getOwnNcPairIdxSet(int ncPairIdx){
        List<Integer> nodeIdxList = ncPairList.get(ncPairIdx).getNodeIdxList(); // It may be a super-NC pair.
        Set<Integer> ownNcPairIdxSet = new HashSet<>();

        for (int nodeIdx : nodeIdxList){
            List<Integer> ownNcPairIdxList = ncPairClusters.get(nodeIdx);

            for (int ownNcPairIdx : ownNcPairIdxList){
                if (unremovedNcPairIdxSet.contains(ownNcPairIdx)){
                    ownNcPairIdxSet.add(ownNcPairIdx); // No duplicates
                }
            }
        }
        
        return ownNcPairIdxSet;
    }
    /**
     * Check if pairIdx2 is a successor of pairIdx1.
     * 
     * v1 = (S1, C1), v2 = (S2, C2)
     * v2 is a successor of v1, if S1 \subseteq S2 and C1 \subset C2.
     * 
     * @param idx1
     * @param idx2
     * @return 
     */
    public boolean checkSuccessor(int idx1, int idx2){
        NodeChannelPair pair1 = ncPairList.get(idx1);
        NodeChannelPair pair2 = ncPairList.get(idx2);

        // Step 1: Check if S1 is a subset of S2
        boolean flag = true;
        Set<Integer> set = new HashSet<>(pair2.getNodeIdxList());
        for (int nodeIdx : pair1.getNodeIdxList()){
            if (!set.contains(nodeIdx)){
                flag = false;
                break;
            }
        }
        
        if (flag == false){
            return false;
        }
        
        // Step 2: Check if C1 is a strict subset of C2
        int startChn1 = pair1.getChannel().getStartChn();
        int endChn1 = pair1.getChannel().getEndChn();
        int startChn2 = pair2.getChannel().getStartChn();
        int endChn2 = pair2.getChannel().getEndChn();
        
        return ( ((startChn2 <= startChn1) && (endChn1 < endChn2)) || 
             ((startChn2 < startChn1)  && (endChn1 <= endChn2)));
    }
    
    public double getReward (int ncPairIdx){
        return ncPairList.get(ncPairIdx).getReward();
    }
    
    public List<Node> getNodeList(){
        return nodeList;
    }
    
    public List<NodeChannelPair> getNcPairList(){
        return ncPairList;
    }
    
    public NodeChannelPair getNcPair(int ncPairIdx){
        return ncPairList.get(ncPairIdx);
    }
    
    public Set<Integer> getUnremovedNcPairIdxSet(){
        return unremovedNcPairIdxSet;
    }
    
    public Set<Integer> getUnremovedMinDemandNcPairIdxSet(){
        return unremovedMinDemandNcPairIdxSet;
    }
    
    public List<Integer> getIntNeighborIdxList(int nodeIdx){
        return intGraph.getNeighborIdxList(nodeIdx);
    }
    
    public List<Integer> getCsNeighborIdxList(int nodeIdx){
        return csGraph.getNeighborIdxList(nodeIdx);
    }
    
    /*
        Reset the graph means setting all NC pairs unremoved. 
    */
    public void reset(){
        unremovedNcPairIdxSet = createUnremovedNcPairIdxSet(numOfNcPairs);
        unremovedMinDemandNcPairIdxSet = createUnremovedMinDemandNcPairIdxSet(minDemandNcPairIdxList);
    }
    
    /**
     * Reset the graph given an index list of selected NC pairs. 
     * 
     * @param selectedNcPairIdxList 
     */
    public void reset(List<Integer> selectedNcPairIdxList){
        unremovedNcPairIdxSet = createUnremovedNcPairIdxSet(numOfNcPairs);
        unremovedMinDemandNcPairIdxSet = createUnremovedMinDemandNcPairIdxSet(minDemandNcPairIdxList);
        
        selectedNcPairIdxList.stream().forEach((idx) -> {
            // Remove non-successor conflicting neighbors of NC pair indexed by idx.
            List<Integer> neighborIdxList = new ArrayList(edgeList.get(idx).keySet());
            neighborIdxList.stream().forEach((neighborIdx) -> {
                if ((neighborIdx != idx) && 
                        checkSuccessor(idx, neighborIdx) == false){
                    unremovedNcPairIdxSet.remove(neighborIdx);
                }
            });
            
            neighborIdxList = minDemandEdgeDict.get(idx);
            neighborIdxList.stream().forEach((neighborIdx) -> {
                if ((neighborIdx != idx) &&
                        checkSuccessor(idx, neighborIdx) == false){
                    unremovedMinDemandNcPairIdxSet.remove(neighborIdx);
                }
            });
        });
    }
    
    /*
        Remove the given NC pair AND its conflicting neigbors from the graph.
    */
    public void removeNcPairFromGraph(int ncPairIdx){
        unremovedNcPairIdxSet.remove(ncPairIdx);
        
        ArrayList<Integer> neighborIdxList = new ArrayList(edgeList.get(ncPairIdx).keySet());
        neighborIdxList.stream().forEach((neighborIdx) -> {
            unremovedNcPairIdxSet.remove(neighborIdx);
        });
    }
    /**
     * Min-demand CA: (1) Remove v and its neighbors in V' from V'; (2) remove v 
     * and its neighbors except its successors in V from V, 
     * where V' is unremovedMinDemandNcPairIdxSet, and V is unremovedNcPairIdxSet.
     * 
     * 
     * @param ncPairIdx 
     */
    public void removeNcPairFromGraph2(int ncPairIdx){
        List<Integer> removedList1 = new ArrayList<>();
        
        unremovedMinDemandNcPairIdxSet.remove(ncPairIdx);
        
        List<Integer> minDemandNeighborIdxList = minDemandEdgeDict.get(ncPairIdx);
        for ( int minDemendNeighborIdx : minDemandNeighborIdxList){
            if (unremovedMinDemandNcPairIdxSet.contains(minDemendNeighborIdx)){
                unremovedMinDemandNcPairIdxSet.remove(minDemendNeighborIdx);
                removedList1.add(minDemendNeighborIdx);
            }
        }
                 
        if (isDebug){
            System.out.println("Removed min-demand NC pairs: " + removedList1.toString());
        }
        
        List<Integer> removedList2 = new ArrayList<>();
        
        unremovedNcPairIdxSet.remove(ncPairIdx);
        
        ArrayList<Integer> neighborIdxList = new ArrayList(edgeList.get(ncPairIdx).keySet());
        neighborIdxList.stream().forEach((neighborIdx) -> {
            if ( unremovedNcPairIdxSet.contains(neighborIdx) && 
                    checkSuccessor(ncPairIdx, neighborIdx) == false){
                unremovedNcPairIdxSet.remove(neighborIdx);
                removedList2.add(neighborIdx);
            }
        });
        
        if (isDebug){
            System.out.println("Removed NC pairs: " + removedList2.toString());
        }
    }
    
    public boolean isRemoved(int ncPairIdx){
        return (!unremovedNcPairIdxSet.contains(ncPairIdx));
    }
       
    public void print(){
        System.out.println("AdjacencyMatrix = ");
        for (int i = 0; i < numOfNcPairs; i++){
            if (unremovedNcPairIdxSet.contains(i)){
                System.out.format("\tIdx = %d, (%s, %s), isMinDemand = %s, Deg = %d, minDemandDeg = %d, exist = %s\n", 
                        i, ncPairList.get(i).getNodeIdxList().toString(), ncPairList.get(i).getChannel().getChnList(),
                        ncPairList.get(i).isMinDemand(), this.getVertexDegree(i), this.getMinDemandVertexDegree(i),
                        this.unremovedNcPairIdxSet.contains(i));
            }
        }
        
        nodeList.stream().forEach((node) -> {
            node.print();
            
            List<Integer> intNeighborIdxList = getIntNeighborIdxList(node.getNodeIdx());
            System.out.println("\t# of interfering nbrs   = " + intNeighborIdxList.size() + 
                    ", nodeIdx: " + intNeighborIdxList.toString());
            
            List<Integer> csNeighborIdxList = getCsNeighborIdxList(node.getNodeIdx());
            System.out.println("\t# of carrier-sense nbrs = " + csNeighborIdxList.size() + 
                    ", nodeIdx: " + csNeighborIdxList.toString());
        });
    }
    
    public void printUnremovedNcPairIdxSet(){
        System.out.println("Remaining NC pair indices: " + 
                new ArrayList<>(unremovedNcPairIdxSet).toString());
    }
    
    public void printUnremovedMinDemandNcPairIdxSet(){
        System.out.println("Remaining min-demand NC pair indices: " + 
                new ArrayList<>(unremovedMinDemandNcPairIdxSet).toString());
    }
}
