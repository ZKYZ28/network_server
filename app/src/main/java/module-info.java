module org.helmo {
    requires com.google.gson;
    opens org.helmo.murmurG6.models to com.google.gson;
    opens org.helmo.murmurG6.infrastructure.dto to com.google.gson;
    opens org.helmo.murmurG6.infrastructure to com.google.gson;
    exports org.helmo.murmurG6;
    opens org.helmo.murmurG6.models.exceptions to com.google.gson;
    opens org.helmo.murmurG6.controller to com.google.gson;
    opens org.helmo.murmurG6.controller.exceptions to com.google.gson;
    opens org.helmo.murmurG6.executor to com.google.gson;
}