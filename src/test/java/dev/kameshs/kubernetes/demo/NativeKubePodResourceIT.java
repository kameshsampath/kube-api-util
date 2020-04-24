package dev.kameshs.kubernetes.demo;

import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
public class NativeKubePodResourceIT extends KubePodResourceTest {

    // Execute the same tests but in native mode.
}