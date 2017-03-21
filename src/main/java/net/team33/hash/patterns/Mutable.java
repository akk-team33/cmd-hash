package net.team33.hash.patterns;

public class Mutable<T> {

    private T subject;

    public static <T> Mutable<T> of(final T subject) {
        return new Mutable<T>().set(subject);
    }

    public T get() {
        return subject;
    }

    public Mutable<T> set(final T subject) {
        this.subject = subject;
        return this;
    }
}
