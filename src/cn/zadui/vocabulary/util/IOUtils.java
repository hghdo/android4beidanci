package cn.zadui.vocabulary.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class IOUtils {
    
    private static final String LOG_TAG = "IOUtils";
    
    public static String getUrlResponse(String url) {
        try {
            HttpGet get = new HttpGet(url);
//            Log.d(LOG_TAG, "url: " + url);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            return convertStreamToString(entity.getContent());
        } catch (Exception e) {
//            Log.e(LOG_TAG, e.getMessage());
        }
        return null;
    }
    
    public static InputStream getUrlResponseAsInputStream(String url) throws IllegalStateException, IOException {
        HttpGet get = new HttpGet(url);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(get);
        HttpEntity entity = response.getEntity();
        return entity.getContent();
    }
    

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8*1024);
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
//            Log.e(LOG_TAG, e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
//                Log.e(LOG_TAG, e.getMessage());
            }
        }
 
        return sb.toString();
    }
    
    public static Bitmap getBitmapFromUrl(URL url) {
         Bitmap bitmap = null;
         InputStream in = null;
         OutputStream out = null;

         try {
             in = new BufferedInputStream(url.openStream(), 4 * 1024);

             final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
             out = new BufferedOutputStream(dataStream, 4 * 1024);
             copy(in, out);
             out.flush();

             final byte[] data = dataStream.toByteArray();
             bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//             Log.e(LOG_TAG, "bitmap returning something");
             return bitmap;
         } catch (IOException e) {
//             Log.e(LOG_TAG, e.getMessage());
         } finally {
             closeStream(in);
             closeStream(out);
         }
//         Log.e(LOG_TAG, "bitmap returning null");
         return null;
    }
    
    public static Drawable getDrawableFromUrl(URL url) {
        try {
            InputStream is = url.openStream();
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        } catch (MalformedURLException e) {
//            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
        }
        return null;
    }
    
    private static void copy(InputStream in, OutputStream out) throws IOException {
       byte[] b = new byte[4 * 1024];
       int read;
       while ((read = in.read(b)) != -1) {
           out.write(b, 0, read);
       }
   }
    
    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
//              Log.e(LOG_TAG, e.getMessage());
            }
        }
    }
}
