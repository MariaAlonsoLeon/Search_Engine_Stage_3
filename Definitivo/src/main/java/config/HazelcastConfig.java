package config;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Arrays;
import java.util.List;

public class HazelcastConfig {

    public static HazelcastInstance initializeHazelcast() {
        Config config = new Config();

        String clusterName = System.getenv("HZ_CLUSTERNAME");
        if (clusterName == null || clusterName.isEmpty()) {
            clusterName = "default-cluster";
        }
        config.setClusterName(clusterName);

        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(5701).setPortAutoIncrement(true);

        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);

        String members = System.getenv("HZ_NETWORK_JOIN_TCPIP_MEMBERS");
        if (members != null && !members.isEmpty()) {
            List<String> memberList = Arrays.asList(members.split(","));
            joinConfig.getTcpIpConfig().setEnabled(true).setMembers(memberList);
        } else {
            joinConfig.getTcpIpConfig().setEnabled(false);
        }

        String publicAddress = System.getenv("HZ_NETWORK_PUBLICADDRESS");
        if (publicAddress != null && !publicAddress.isEmpty()) {
            networkConfig.setPublicAddress(publicAddress);
        }

        config.getMapConfig("datalake").setBackupCount(1);
        config.getMapConfig("invertedIndex").setBackupCount(1);
        config.getMapConfig("metadata").setBackupCount(1);

        return Hazelcast.newHazelcastInstance(config);
    }
}
