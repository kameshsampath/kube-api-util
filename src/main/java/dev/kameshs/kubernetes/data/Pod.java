package dev.kameshs.kubernetes.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class Pod {

	public String name;
	public String namespace;
	public List<KeyValue> labels = new ArrayList<>();
	public List<String> containers = new ArrayList<>();

	public Pod() {

	}

	Pod(String name, String namespace, Map<String, String> labels,
			List<String> containers) {
		this.name = name;
		this.namespace = namespace;
		listFromMap(labels);
		this.containers = containers;
	}

	private void listFromMap(Map<String, String> labels) {
		labels.forEach((k, v) -> this.labels.add(new KeyValue(k, v)));
	}

	public static Map<String, String> mapFromLabels(
			List<KeyValue> labels) {
		return labels.stream()
				.collect(Collectors.toMap(l -> l.key, l -> l.value));
	}

	public static Pod newPod(io.fabric8.kubernetes.api.model.Pod pod) {
		ObjectMeta metadata = pod.getMetadata();
		List<String> containers =
				pod.getSpec().getContainers().stream().map(c -> c.getImage())
						.collect(Collectors.toList());
		return new Pod(metadata.getName(), metadata.getNamespace(),
				metadata.getLabels(), containers);
	}
}
