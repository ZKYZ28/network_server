module org.helmo {
    requires com.google.gson;
    opens org.helmo.murmurG6.models to com.google.gson;
    exports org.helmo.murmurG6;
}
