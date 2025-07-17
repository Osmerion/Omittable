import org.jspecify.annotations.NullMarked;

@NullMarked
module com.osmerion.omittable.jackson {

    requires transitive com.osmerion.omittable;
    requires transitive org.jspecify;

    requires static com.fasterxml.jackson.databind;

    exports com.osmerion.omittable.jackson;

}
