package alv.splash.browser;

import android.content.Context;
import android.graphics.Bitmap;
import  com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;

import java.util.concurrent.ExecutionException;

public class GlideFaviconFetcher {

    public interface FaviconCallback {
        void onFaviconLoaded(Bitmap bitmap);
        void onError(Exception e);
    }

    public static void fetchFavicon(Context context, String domain, FaviconCallback callback) {
        new Thread(() -> {
            try {
                String faviconUrl = "https://www.google.com/s2/favicons?sz=64&domain=" + domain;
                FutureTarget<Bitmap> futureTarget = Glide.with(context)
                        .asBitmap()
                        .load(faviconUrl)
                        .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

                Bitmap bitmap = futureTarget.get(); // Download di background
                callback.onFaviconLoaded(bitmap);
            } catch (ExecutionException | InterruptedException e) {
                callback.onError(e);
            }
        }).start();
    }
}
