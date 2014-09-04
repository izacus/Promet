package si.virag.promet.utils;

import rx.Subscriber;

/**
 * Used to make code less verbose
 */
public class SubscriberAdapter<T> extends Subscriber<T> {
    @Override
    public void onCompleted() {
        // Empty
    }

    @Override
    public void onError(Throwable throwable) {
        // Empty
    }

    @Override
    public void onNext(T t) {
        // Empty
    }
}
