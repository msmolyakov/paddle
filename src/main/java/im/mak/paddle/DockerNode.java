package im.mak.paddle;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import im.mak.paddle.exceptions.NodeError;

import java.util.*;

public class DockerNode extends Node {

    private DockerClient docker;
    private String containerId;

    public DockerNode(String image, String tag, int apiPort, char chainId, String richSeed,
                      int blockWaitingInSeconds, int transactionWaitingInSeconds) {
        super("http://127.0.0.1:" + apiPort, chainId, richSeed);
        this.blockWaitingInSeconds = blockWaitingInSeconds;
        this.transactionWaitingInSeconds = transactionWaitingInSeconds;

        try {
            String imageNameAndTag = image + ":" + tag;

            this.docker = DefaultDockerClient.fromEnv().build();
            if (this.docker.listImages(DockerClient.ListImagesParam.byName(imageNameAndTag)).size() < 1)
                this.docker.pull(imageNameAndTag);

            String[] ports = {"6860", String.valueOf(apiPort)}; //TODO remove 6860 - not used
            Map<String, List<PortBinding>> portBindings = new HashMap<>();
            for (String port : ports) { // TODO randomly allocated?
                List<PortBinding> hostPorts = new ArrayList<>();
                hostPorts.add(PortBinding.of("0.0.0.0", port));
                portBindings.put(port, hostPorts);
            }

            HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

            ContainerConfig containerConfig = ContainerConfig.builder()
                    .hostConfig(hostConfig)
                    .image(imageNameAndTag)
                    .exposedPorts(ports)
                    .build();

            ContainerCreation container = this.docker.createContainer(containerConfig);
            this.containerId = container.id();

            this.docker.startContainer(this.containerId);

            //wait node readiness
            boolean isNodeReady = false;
            for (int repeat = 0; repeat < 60; repeat++) {
                try {
                    api.version();
                    isNodeReady = true;
                    break;
                } catch (NodeError e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {}
                }
            }
            if (!isNodeReady) throw new NodeError("Could not wait for node readiness");
        } catch (DockerException | DockerCertificateException | InterruptedException e) {
            throw new NodeError(e);
        }
    }

    public DockerNode(String image, String tag, int apiPort, char chainId, String richSeed) {
        this(image, tag, apiPort, chainId, richSeed, 30, 10);
    }

    public DockerNode() {
        this("wavesplatform/waves-private-node", "latest", 6869, 'R',
                "waves private node seed with waves tokens");
    }

    public void shutdown() {
        try {
            if (docker != null && docker.listContainers().stream().anyMatch(c -> c.id().equals(containerId))) {
                docker.killContainer(containerId);
                docker.removeContainer(containerId);
                docker.close();
            }
        } catch (DockerException | InterruptedException e) {
            throw new NodeError(e);
        }
    }

}
