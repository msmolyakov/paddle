package im.mak.paddle;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.wavesplatform.wavesj.Transaction;
import im.mak.paddle.api.Api;
import im.mak.paddle.exceptions.NodeError;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {

    DockerClient docker;
    String containerId = "";
    public com.wavesplatform.wavesj.Node wavesNode;
    public Account rich;

    public Api api;

    public static Node connectToNode(String uri, char chainId) {
        try {
            Node node = new Node();
            node.wavesNode = new com.wavesplatform.wavesj.Node(uri, chainId);

            node.api = new Api(node.wavesNode.getUri());

            node.rich = new Account("create genesis wallet devnet-0-d", node);
            return node;
        } catch (URISyntaxException e) {
            throw new NodeError(e);
        }
    }

    public static Node runDockerNode(Version version) {
        try {
            Node node = new Node();
            String tag = version == Version.MAINNET ? "latest" : "testnet"; //TODO latest or specific version

            node.docker = new DefaultDockerClient("unix:///var/run/docker.sock");
            node.docker.pull("wavesplatform/waves-private-node:" + tag);

            String[] ports = {"6860", "6869"};
            Map<String, List<PortBinding>> portBindings = new HashMap<>();
            for (String port : ports) { // TODO randomly allocated?
                List<PortBinding> hostPorts = new ArrayList<>();
                hostPorts.add(PortBinding.of("0.0.0.0", port));
                portBindings.put(port, hostPorts);
            }

            HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

            ContainerConfig containerConfig = ContainerConfig.builder()
                    .hostConfig(hostConfig)
                    .image("wavesplatform/waves-private-node:" + tag).exposedPorts(ports)
                    .build();

            ContainerCreation container = node.docker.createContainer(containerConfig);
            node.containerId = container.id();

            node.docker.startContainer(node.containerId);

            node.wavesNode = new com.wavesplatform.wavesj.Node("http://127.0.0.1:6869", 'R');
            node.api = new Api(node.wavesNode.getUri());

            node.rich = new Account("waves private node seed with waves tokens", node);

            //wait node readiness
            boolean isNodeReady = false;
            Thread.sleep(8000);
            for (int repeat = 0; repeat < 6; repeat++) {
                try {
                    node.version();
                    isNodeReady = true;
                    break;
                } catch (NodeError e) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignore) {}
                }
            }
            if (!isNodeReady) throw new NodeError("Could not wait for node readiness");

            return node;
        } catch (URISyntaxException | DockerException | InterruptedException e) {
            throw new NodeError(e);
        }
    }

    private String version() {
        try {
            return wavesNode.getVersion();
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public int height() {
        try {
            return wavesNode.getHeight();
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public void stopDockerNode() {
        try {
            docker.killContainer(containerId);
            docker.removeContainer(containerId);
            docker.close();
        } catch (DockerException | InterruptedException e) {
            throw new NodeError(e);
        }
    }

    public boolean isSmart(String assetIdOrAddress) {
        if (assetIdOrAddress == null || assetIdOrAddress.isEmpty() || "WAVES".equals(assetIdOrAddress))
            return false;
        else if (assetIdOrAddress.length() > 40) {
            return api.assetDetails(assetIdOrAddress).scripted;
        } else {
            return api.scriptInfo(assetIdOrAddress).extraFee > 0;
        }
    }

    public boolean isSmart(Account account) {
        return isSmart(account.address());
    }

    public Transaction waitForTransaction(String id) {
        for (int repeat = 0; repeat < 100; repeat++) {
            try {
                return wavesNode.getTransaction(id);
            } catch (IOException e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
        }
        throw new NodeError("Could not wait for transaction " + id + " in 10 seconds");
    }

    public int waitForHeight(int target, long durationInMilliseconds) {
        int current = 0;
        int pollingInterval = 1000;
        for (int timeSpent = 0; timeSpent < durationInMilliseconds; timeSpent += pollingInterval) {
            try {
                current = height();
                if (current >= target)
                    return current;
            } catch (NodeError ignored) {}

            if (timeSpent + pollingInterval < durationInMilliseconds)
                try {
                    Thread.sleep(pollingInterval);
                } catch (InterruptedException ignored) {}
        }
        throw new NodeError("Could not wait for height " + target + " in " + (durationInMilliseconds/1000) +
                " seconds. Current height: " + current);
    }

    public int waitForHeight(int expectedHeight) {
        return waitForHeight(expectedHeight, 60_000);
    }

    public int waitNBlocks(int blocksCount, long durationInMilliseconds) {
        return waitForHeight(height() + blocksCount, durationInMilliseconds);
    }

    public int waitNBlocks(int blocksCount) {
        return waitNBlocks(blocksCount, 60_000);
    }

    public byte chainId() {
        return wavesNode.getChainId();
    }

    public String compileScript(String s) {
        try {
            return wavesNode.compileScript(s);
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

}
