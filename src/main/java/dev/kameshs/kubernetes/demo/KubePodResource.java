package dev.kameshs.kubernetes.demo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import dev.kameshs.kubernetes.data.Pod;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;

@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KubePodResource {

    @Inject
    KubernetesClient client;

    @GET
    @Path("/pods/{namespace}")
    public Response pods(@NotBlank @PathParam("namespace") String namespace) {

        PodList pods = client.pods().inNamespace(namespace)
                .list();
        List<Pod> podNames = pods.getItems().stream()
                .map(Pod::newPod)
                .collect(Collectors.toList());
        return Response.ok().entity(podNames).build();
    }

    @POST
    @Path("/pods/{namespace}")
    public Response addPod(@NotBlank @PathParam("namespace") String namespace,
            Pod podSpec) {

        AtomicInteger i = new AtomicInteger();
        List<Container> containers = podSpec.containers
                .stream()
                .map(c -> {
                    ContainerBuilder cb = new ContainerBuilder();
                    ContainerPortBuilder cPortBuilder =
                            new ContainerPortBuilder();
                    ContainerPort cp =
                            cPortBuilder.withNewName("http")
                                    .withProtocol("TCP")
                                    .withContainerPort(8080).build();
                    return cb.withImage(c)
                            .withName("user-container-" + i.incrementAndGet())
                            .withPorts(cp)
                            .build();
                }).collect(Collectors.toList());


        final io.fabric8.kubernetes.api.model.Pod newPod =
                client.pods().inNamespace(namespace)
                        .createNew()
                        .withNewMetadata()
                        .withName(podSpec.name)
                        .withLabels(Pod.mapFromLabels(podSpec.labels))
                        .endMetadata()
                        .withNewSpec()
                        .withContainers(containers)
                        .endSpec()
                        .done();
        return Response.ok().entity(newPod).build();
    }

    @DELETE
    @Path("/pods/{namespace}/{name}")
    public Response deletePod(
            @NotBlank @PathParam("namespace") String namespace,
            @NotBlank @PathParam("name") String name) {
        boolean isDeleted =
                client.pods().inNamespace(namespace).withName(name).delete();
        Status status = Status.NO_CONTENT;
        if (!isDeleted) {
            status = Status.INTERNAL_SERVER_ERROR;
        }
        return Response.status(status).build();
    }


}
