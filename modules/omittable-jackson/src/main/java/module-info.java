module com.osmerion.omittable.jackson {

    requires transitive com.osmerion.omittable;
    requires static com.fasterxml.jackson.databind;

    exports com.osmerion.omittable.jackson;

}
